package com.hahaen.ledger.item.service;

import com.hahaen.ledger.common.exception.BusinessException;
import com.hahaen.ledger.common.security.AuditSupport;
import com.hahaen.ledger.common.security.CurrentUser;
import com.hahaen.ledger.item.dto.*;
import com.hahaen.ledger.item.entity.PersonalItem;
import com.hahaen.ledger.item.mapper.PersonalItemMapper;
import com.hahaen.ledger.item.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class ItemService {
    private static final LocalDate ITEM_MIN_DATE = LocalDate.of(2000, 1, 1);
    private final PersonalItemMapper mapper;
    @Value("${hahaen.timezone:Asia/Shanghai}")
    private String timezone = "Asia/Shanghai";
    private LocalDate today() { return LocalDate.now(ZoneId.of(timezone)); }

    public ItemOverviewVO overview(String status, int page, int pageSize) {
        if (!List.of("ACTIVE", "RETIRED", "ALL").contains(status) || page < 1 || pageSize < 1 || pageSize > 100)
            throw new BusinessException("ITEM_QUERY_INVALID", "物品筛选或分页参数不正确");
        LocalDate date = today();
        List<ItemVO> all = mapper.listOwned(CurrentUser.id()).stream().map(item -> ItemCosts.view(item, date)).toList();
        var active = all.stream().filter(item -> "ACTIVE".equals(item.status())).toList();
        long assets = active.stream().mapToLong(ItemVO::priceCents).reduce(0, Math::addExact);
        BigDecimal daily = active.stream().map(ItemVO::dailyCostCents).reduce(BigDecimal.ZERO, BigDecimal::add);
        var filtered = all.stream().filter(item -> "ALL".equals(status) || status.equals(item.status())).toList();
        long offset = (long) (page - 1) * pageSize;
        var items = filtered.stream().skip(offset).limit(pageSize).toList();
        return new ItemOverviewVO(date, assets, daily, active.size(), all.size() - active.size(), items,
            filtered.size(), page, pageSize, offset + items.size() < filtered.size());
    }

    public ItemDetailVO detail(long id) {
        PersonalItem item = mapper.owned(CurrentUser.id(), id);
        if (item == null) throw missing();
        LocalDate date = today();
        return new ItemDetailVO(ItemCosts.view(item, date), date, ItemCosts.history(item, date));
    }

    @Transactional
    public ItemVO create(ItemRequest request) {
        long user = lockedUser();
        String name = request.name() == null ? "" : request.name().trim();
        if (name.isEmpty() || name.codePointCount(0, name.length()) > 40)
            throw new BusinessException("ITEM_NAME_INVALID", "物品名称需为1至40个字符");
        amount(request.priceCents());
        date(request.purchasedOn());
        key(request.idempotencyKey());
        if (request.serving() == null) throw new BusinessException("ITEM_STATUS_REQUIRED", "请选择服役状态");
        if (request.serving() && (request.retiredOn() != null || request.resaleCents() != null))
            throw new BusinessException("ITEM_RETIRE_INVALID", "在役物品不能填写退役信息");
        if (!request.serving()) retirement(request.purchasedOn(), request.retiredOn(), request.resaleCents());
        PersonalItem existing = mapper.byCreateKey(user, request.idempotencyKey());
        if (existing != null) {
            boolean same = Objects.equals(existing.getCreateHash(), createHash(request, name));
            if (!same) throw conflict();
            if (!Integer.valueOf(0).equals(existing.getDeleted())) throw missing();
            return ItemCosts.view(existing, today());
        }
        PersonalItem item = new PersonalItem();
        item.setUserId(user); item.setName(name); item.setPriceCent(request.priceCents());
        item.setPurchasedOn(request.purchasedOn()); item.setStatus(request.serving() ? "ACTIVE" : "RETIRED");
        item.setRetiredOn(request.retiredOn()); item.setResaleCent(request.resaleCents());
        item.setCreateKey(request.idempotencyKey()); item.setCreateHash(createHash(request, name));
        mapper.insert(item);
        return ItemCosts.view(item, today());
    }

    @Transactional
    public ItemVO retire(long id, RetireItemRequest request) {
        long user = lockedUser();
        key(request.idempotencyKey());
        PersonalItem item = mapper.ownedForUpdate(user, id);
        if (item == null || !Integer.valueOf(0).equals(item.getDeleted())) throw missing();
        retirement(item.getPurchasedOn(), request.retiredOn(), request.resaleCents());
        if ("RETIRED".equals(item.getStatus())) {
            if (!Objects.equals(item.getRetireKey(), request.idempotencyKey())
                || !Objects.equals(item.getRetiredOn(), request.retiredOn()) || !Objects.equals(item.getResaleCent(), request.resaleCents())) throw conflict();
            return ItemCosts.view(item, today());
        }
        // 重新服役后延迟到达的上一次退役请求不能再次改变状态。
        if (Objects.equals(item.getRetireKey(), request.idempotencyKey())) throw conflict();
        item.setStatus("RETIRED"); item.setRetiredOn(request.retiredOn()); item.setResaleCent(request.resaleCents());
        item.setRetireKey(request.idempotencyKey());
        mapper.updateById(item);
        return ItemCosts.view(item, today());
    }

    @Transactional
    public ItemVO reactivate(long id, ReactivateItemRequest request) {
        long user = lockedUser();
        key(request.idempotencyKey());
        PersonalItem item = mapper.ownedForUpdate(user, id);
        if (item == null || !Integer.valueOf(0).equals(item.getDeleted())) throw missing();
        Long previousItemId = mapper.reactivateRequestItemId(user, request.idempotencyKey());
        if (previousItemId != null) {
            if (previousItemId != id || !"ACTIVE".equals(item.getStatus())) throw conflict();
            return ItemCosts.view(item, today());
        }
        if (!"RETIRED".equals(item.getStatus())) throw conflict();
        if (mapper.reactivateOwned(user, id, CurrentUser.optionalName()) != 1) throw conflict();
        if (mapper.insertReactivateRequest(user, request.idempotencyKey(), id) != 1) throw conflict();
        item.setStatus("ACTIVE"); item.setRetiredOn(null); item.setResaleCent(null);
        return ItemCosts.view(item, today());
    }

    @Transactional
    public ItemVO edit(long id, EditItemRequest request) {
        long user = lockedUser();
        key(request.idempotencyKey());
        String name = request.name() == null ? "" : request.name().trim();
        if (name.isEmpty() || name.codePointCount(0, name.length()) > 40)
            throw new BusinessException("ITEM_NAME_INVALID", "物品名称需为1至40个字符");
        amount(request.priceCents());
        date(request.purchasedOn());
        PersonalItem item = mapper.ownedForUpdate(user, id);
        if (item == null || !Integer.valueOf(0).equals(item.getDeleted())) throw missing();
        if ("RETIRED".equals(item.getStatus()) && request.purchasedOn().isAfter(item.getRetiredOn()))
            throw new BusinessException("ITEM_DATE_INVALID", "购买日期不能晚于退役日期");
        String hash = editHash(id, name, request.priceCents(), request.purchasedOn());
        String previousHash = mapper.editRequestHash(user, request.idempotencyKey());
        if (previousHash != null) {
            if (!Objects.equals(previousHash, hash)) throw conflict();
            return ItemCosts.view(item, today());
        }
        item.setName(name);
        item.setPriceCent(request.priceCents());
        item.setPurchasedOn(request.purchasedOn());
        if (mapper.updateById(item) != 1) throw conflict();
        mapper.insertEditRequest(user, request.idempotencyKey(), id, hash);
        return ItemCosts.view(item, today());
    }

    @Transactional
    public void delete(long id, String requestKey) {
        long user = lockedUser(); key(requestKey);
        PersonalItem item = mapper.ownedForUpdate(user, id);
        if (item == null) throw missing();
        if (!Integer.valueOf(0).equals(item.getDeleted())) {
            if (!Objects.equals(item.getDeleteKey(), requestKey)) throw missing();
            return;
        }
        AuditSupport.markDeleted(item); item.setDeleteKey(requestKey);
        if (mapper.softDelete(item) != 1) throw conflict();
    }

    private static String createHash(ItemRequest request, String name) {
        String payload = name.length() + ":" + name + ":" + request.priceCents() + ":" + request.purchasedOn()
            + ":" + request.serving() + ":" + request.retiredOn() + ":" + request.resaleCents();
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) { throw new IllegalStateException("SHA-256 unavailable", exception); }
    }

    private static String editHash(long id, String name, long priceCents, LocalDate purchasedOn) {
        String payload = id + ":" + name.length() + ":" + name + ":" + priceCents + ":" + purchasedOn;
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) { throw new IllegalStateException("SHA-256 unavailable", exception); }
    }

    private long lockedUser() {
        long user = CurrentUser.id();
        if (mapper.lockUser(user) == null) throw new BusinessException("AUTH_REQUIRED", "用户不可用，请重新登录");
        return user;
    }
    private void retirement(LocalDate bought, LocalDate retired, Long resale) {
        date(retired); amount(resale);
        if (retired.isBefore(bought)) throw new BusinessException("ITEM_DATE_INVALID", "退役日期不能早于购买日期");
    }
    private void date(LocalDate date) {
        if (date == null || date.isBefore(ITEM_MIN_DATE) || date.isAfter(today()))
            throw new BusinessException("ITEM_DATE_INVALID", "日期须在2000-01-01至今天之间");
    }
    private static void amount(Long value) {
        if (value == null || value < 0 || value > 99_999_999_999L)
            throw new BusinessException("ITEM_AMOUNT_INVALID", "金额须在0至999,999,999.99元之间");
    }
    private static void key(String key) {
        if (key == null || !key.matches("[A-Za-z0-9_-]{16,64}"))
            throw new BusinessException("ITEM_KEY_INVALID", "请求标识格式不正确");
    }
    private static BusinessException missing() { return new BusinessException("ITEM_NOT_FOUND", "物品不存在或已删除"); }
    private static BusinessException conflict() { return new BusinessException("IDEMPOTENCY_CONFLICT", "物品状态或请求内容已变化，请刷新后重试"); }
}
