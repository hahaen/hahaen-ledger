package com.hahaen.ledger.lottery.service;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hahaen.ledger.lottery.entity.LotteryDraw;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.time.*;
import java.util.*;

@Component
public class LotteryOfficialClient {
    private final RestClient http;
    private final ObjectMapper json;
    public LotteryOfficialClient(ObjectMapper json) {
        this.json=json;
        var f=new SimpleClientHttpRequestFactory();f.setConnectTimeout(5000);f.setReadTimeout(12000);
        http=RestClient.builder().requestFactory(f).defaultHeader("User-Agent","Mozilla/5.0").build();
    }
    public record Item(LotteryDraw draw,String raw) {}
    public record Page(List<Item> items,int pages) {}
    public Page fetch(String game,int page) {
        Page current=fetchPage(game,page);
        if(page>1&&!current.items().isEmpty()) {
            Page previous=fetchPage(game,page-1);
            LotteryDraw first=current.items().getFirst().draw();
            String candidate=null;
            for(int i=0;i<previous.items().size();i++) {
                LotteryDraw d=previous.items().get(i).draw();
                if(d.getIssue().equals(first.getIssue())&&i>0){candidate=previous.items().get(i-1).draw().getIssue();break;}
            }
            if(candidate==null&&!previous.items().isEmpty())candidate=previous.items().getLast().draw().getIssue();
            if(candidate!=null&&adjacent(first.getIssue(),candidate))first.setNextIssue(candidate);
        }
        return current;
    }
    /** 仅校验官方相邻页返回的期号，不生成任何未来期号。 */
    static boolean adjacent(String older,String newer) {
        String oldYear=older.substring(0,older.length()-3),newYear=newer.substring(0,newer.length()-3);
        int oldSeq=Integer.parseInt(older.substring(older.length()-3)),newSeq=Integer.parseInt(newer.substring(newer.length()-3));
        return oldYear.equals(newYear)?newSeq==oldSeq+1:Integer.parseInt(newYear)==Integer.parseInt(oldYear)+1&&newSeq==1;
    }
    private Page fetchPage(String game,int page) {
        LotteryRules.game(game);
        if(page<1||page>10000) throw new IllegalArgumentException("Invalid page");
        boolean dlt="DLT".equals(game);
        String url=dlt?"https://webapi.sporttery.cn/gateway/lottery/getHistoryPageListV1.qry?gameNo=85&provinceId=0&pageSize=30&isVerify=1&pageNo="+page:
                "https://www.cwl.gov.cn/cwl_admin/front/cwlkj/search/kjxx/findDrawNotice?name=ssq&issueCount=&pageNo="+page+"&pageSize=30&systemType=PC";
        byte[] payload=http.get().uri(url).header("Referer",dlt?"https://www.sporttery.cn/":"https://www.cwl.gov.cn/ygkj/wqkjgg/ssq/").retrieve().body(byte[].class);
        JsonNode response;
        try {response=payload==null?null:json.readTree(payload);}
        catch(java.io.IOException ex){throw new IllegalStateException("Official JSON invalid");}
        if(response==null||(dlt?!response.path("success").asBoolean():response.path("state").asInt(-1)!=0)) throw new IllegalStateException("Official response invalid");
        JsonNode data=dlt?response.path("value"):response;
        JsonNode rows=data.path(dlt?"list":"result");
        if(!rows.isArray()||rows.size()>30)throw new IllegalStateException("Official list invalid");
        List<Item> items=new ArrayList<>();
        String next=null;
        for(JsonNode row:rows) {
            Item item=parse(game,row);item.draw().setNextIssue(next);next=item.draw().getIssue();items.add(item);
        }
        return new Page(List.copyOf(items),data.path(dlt?"pages":"pageNum").asInt(1));
    }
    public Item parse(String game,JsonNode row) {
        LotteryRules.game(game);boolean dlt="DLT".equals(game);
        LotteryDraw d=new LotteryDraw();d.setGame(game);d.setIssue(LotteryRules.issue(game,row.path(dlt?"lotteryDrawNum":"code").asText()));
        String date=row.path(dlt?"lotteryDrawTime":"date").asText();
        d.setDrawDate(LocalDate.parse(date.substring(0,10)));
        if(d.getDrawDate().isAfter(LocalDate.now(LotteryRules.ZONE)))throw new IllegalArgumentException("Future draw");
        if(dlt) {
            String[] n=row.path("lotteryDrawResult").asText().trim().split("\\s+");
            if(n.length!=7)throw new IllegalArgumentException("DLT numbers invalid");
            d.setRed(LotteryRules.red(game,String.join(",",Arrays.copyOfRange(n,0,5))));
            d.setBlue(LotteryRules.blue(game,String.join(",",Arrays.copyOfRange(n,5,7))));
        }else {d.setRed(LotteryRules.red(game,row.path("red").asText()));d.setBlue(LotteryRules.blue(game,row.path("blue").asText()));}
        ObjectNode prizes=json.createObjectNode();
        for(JsonNode p:row.path(dlt?"prizeLevelList":"prizegrades")) {
            String key;
            if(dlt) {
                String label=p.path("prizeLevel").asText();int grade=0;
                for(int i=1;i<=9;i++)if(label.startsWith(List.of("","一","二","三","四","五","六","七","八","九").get(i)+"等奖")) {grade=i;break;}
                if(grade==0)continue;key=(label.contains("追加")?"A":"")+grade;
            }else key=p.path("type").asText();
            Long cents=LotteryRules.money(p.path(dlt?"stakeAmountFormat":"typemoney").asText());
            if(cents!=null)prizes.put(key,cents);
        }
        d.setPrizes(prizes.toString());
        d.setPoolCents(LotteryRules.money(row.path(dlt?"poolBalanceAfterdraw":"poolmoney").asText()));
        d.setSpecialState(!dlt&&d.getIssue().compareTo("2026014")>=0?(prizes.has("7")&&prizes.path("7").asLong()>0?"ON":"UNKNOWN"):"OFF");
        boolean promotion=dlt?(row.path("lotteryPromotionFlag").asInt()!=0||row.path("lotteryPromotionFlagRj").asInt()!=0):
                !row.path("prizeSpecialInfo").asText().isBlank()||!row.path("addmoney").asText().isBlank()||!row.path("addmoney2").asText().isBlank()||!row.path("compLimitInfo").asText().isBlank();
        d.setPromotion(promotion?1:0);
        d.setSource(dlt?"https://webapi.sporttery.cn":"https://www.cwl.gov.cn");
        d.setRuleVersion(dlt?(d.getIssue().compareTo("26014")>=0?"DLT_2026_26014":d.getIssue().compareTo("19019")>=0?"DLT_2019":"DLT_LEGACY_REVIEW"):d.getIssue().compareTo("2026014")>=0?"SSQ_2026_2026014":"SSQ_CLASSIC");
        // 排除日常浏览无关字段，原始响应仍单独留档；序列化固定顺序保证摘要稳定。
        d.setSourceHash(LotteryRules.hash(d.getIssue()+"|"+d.getDrawDate()+"|"+d.getRed()+"|"+d.getBlue()+"|"+d.getPrizes()+"|"+d.getPoolCents()+"|"+promotion+"|"+row.path("specialRuleInfo")));
        d.setSyncedAt(LotteryRules.now());d.setVersion(1);
        return new Item(d,row.toString());
    }
}
