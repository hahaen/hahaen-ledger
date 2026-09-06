package com.hahaen.ledger.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hahaen.ledger.auth.vo.LoginVO;
import com.hahaen.ledger.book.entity.LedgerBook;
import com.hahaen.ledger.book.mapper.LedgerBookMapper;
import com.hahaen.ledger.common.exception.BusinessException;
import com.hahaen.ledger.user.entity.AppUser;
import com.hahaen.ledger.user.entity.UserIdentity;
import com.hahaen.ledger.user.mapper.AppUserMapper;
import com.hahaen.ledger.user.mapper.UserIdentityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service @RequiredArgsConstructor
public class WechatLoginService {
    private final AppUserMapper userMapper;
    private final UserIdentityMapper identityMapper;
    private final LedgerBookMapper bookMapper;
    private final WechatApiClient wechatApiClient;
    @Value("${hahaen.wechat.login-enabled:false}") private boolean wechatLoginEnabled;
    @Transactional
    public LoginVO login(String code) {
        String openId;
        String unionId = null;
        if (wechatLoginEnabled) { var session = wechatApiClient.exchangeCode(code); openId = session.openId(); unionId = session.unionId(); }
        else openId = code.startsWith("dev-") ? code : "dev-" + code;
        UserIdentity identity = identityMapper.selectOne(new LambdaQueryWrapper<UserIdentity>().eq(UserIdentity::getProvider, "WECHAT_MINI_PROGRAM").eq(UserIdentity::getOpenId, openId));
        AppUser user; LocalDateTime now = LocalDateTime.now();
        if (identity == null) {
            user = new AppUser(); user.setNickname("账本主人"); user.setCreatedName(user.getNickname()); userMapper.insert(user);
            identity = new UserIdentity(); identity.setUserId(user.getId()); identity.setProvider("WECHAT_MINI_PROGRAM"); identity.setOpenId(openId); identity.setUnionId(unionId); identityMapper.insert(identity);
            LedgerBook book = new LedgerBook(); book.setUserId(user.getId()); book.setName("我的账本"); book.setCurrency("CNY"); book.setTimezone("Asia/Shanghai"); book.setStatus("ACTIVE"); book.setCreatedBy(user.getId()); book.setCreatedName(user.getNickname()); bookMapper.insert(book);
        } else user = userMapper.selectById(identity.getUserId());
        StpUtil.login(user.getId());
        StpUtil.getSession().set("auditName", user.getNickname());
        return new LoginVO(StpUtil.getTokenValue(), user.getId(), user.getNickname());
    }
}
