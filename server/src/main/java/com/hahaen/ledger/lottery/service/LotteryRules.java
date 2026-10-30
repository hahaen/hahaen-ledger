package com.hahaen.ledger.lottery.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.hahaen.ledger.common.exception.BusinessException;
import com.hahaen.ledger.lottery.entity.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/** 纯函数兑奖；未知金额与未中奖严格区分。官方当期奖金为准。 */
public final class LotteryRules {
    public static final ZoneId ZONE=ZoneId.of("Asia/Shanghai");
    private LotteryRules() {}
    public static LocalDateTime now() {return LocalDateTime.now(ZONE);}
    public static String game(String game) {
        if(!Set.of("DLT","SSQ").contains(game)) throw error("彩种无效"); return game;
    }
    public static String issue(String game,String issue) {
        game(game);
        if(issue==null || !issue.matches("DLT".equals(game)?"[0-9]{5}":"[0-9]{7}") || Integer.parseInt(issue.substring(issue.length()-3))<1)
            throw error("请填写票面官方期号，大乐透5位，双色球7位");
        return issue;
    }
    public static String numbers(String value,int count,int max) {
        if(value==null || value.isBlank()) throw error("请填写完整号码");
        String[] parts=value.trim().split("[,，\\s]+");
        if(parts.length!=count) throw error("号码数量不正确");
        SortedSet<Integer> nums=new TreeSet<>();
        for(String p:parts) {
            if(!p.matches("[0-9]{1,2}")) throw error("号码格式不正确");
            int n=Integer.parseInt(p); if(n<1||n>max||!nums.add(n)) throw error("号码重复或超出范围");
        }
        return nums.stream().map(n->String.format(Locale.ROOT,"%02d",n)).collect(Collectors.joining(","));
    }
    public static String red(String game,String value) { return numbers(value,"DLT".equals(game)?5:6,"DLT".equals(game)?35:33); }
    public static String blue(String game,String value) { return numbers(value,"DLT".equals(game)?2:1,"DLT".equals(game)?12:16); }
    public static long cost(String game,int multiple,boolean additional) {
        game(game); if(multiple<1||multiple>99||additional&&!"DLT".equals(game)) throw error("投注倍数或追加选项无效");
        return Math.multiplyExact(additional?300L:200L,multiple);
    }
    public static Long money(String value) {
        if(value==null||value.isBlank()||value.equals("-")) return null;
        try {long n=new BigDecimal(value.replace(",","").trim()).movePointRight(2).longValueExact(); return n>=0?n:null;}
        catch(ArithmeticException|NumberFormatException e) {return null;}
    }
    public static String hash(String value) {
        try {return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}
        catch(java.security.NoSuchAlgorithmException e) {throw new IllegalStateException(e);}
    }
    public static int hits(String selected,String drawn) {
        Set<String> set=Set.of(drawn.split(","));return (int)Arrays.stream(selected.split(",")).filter(set::contains).count();
    }
    public record Result(String status,String grade,Long cents) {}
    public static Result settle(LotteryEntry e,LotteryDraw d,JsonNode prizes) {
        if("DLT".equals(e.getGame())&&d.getIssue().compareTo("19019")<0)return new Result("REVIEW","早期历史规则待核实",null);
        int r=hits(e.getRed(),d.getRed()),b=hits(e.getBlue(),d.getBlue());
        int grade=0;
        if("DLT".equals(e.getGame())) {
            boolean modern=d.getIssue().compareTo("26014")>=0;
            if(r==5&&b==2)grade=1;
            else if(r==5&&b==1)grade=2;
            else if(modern) {
                if(r==5||r==4&&b==2)grade=3;
                else if(r==4&&b==1)grade=4;
                else if(r==4||r==3&&b==2)grade=5;
                else if(r==3&&b==1||r==2&&b==2)grade=6;
                else if(r==3||r==2&&b==1||b==2)grade=7;
            } else {
                if(r==5)grade=3;else if(r==4&&b==2)grade=4;else if(r==4&&b==1)grade=5;
                else if(r==3&&b==2)grade=6;else if(r==4)grade=7;else if(r==3&&b==1||r==2&&b==2)grade=8;
                else if(r==3||r==2&&b==1||b==2)grade=9;
            }
        } else {
            if(r==6)grade=b==1?1:2;else if(r==5)grade=b==1?3:4;
            else if(r==4)grade=b==1?4:5;else if(r==3&&b==1)grade=5;else if(b==1)grade=6;
            else if(r==3&&d.getIssue().compareTo("2026014")>=0) {
                if("UNKNOWN".equals(d.getSpecialState()))return new Result("REVIEW","福运奖状态待核实",null);
                if("ON".equals(d.getSpecialState()))grade=7;
            }
        }
        if(grade==0)return new Result("SETTLED","未中奖",0L);
        String label=grade==7&&"SSQ".equals(e.getGame())?"福运奖":List.of("","一等奖","二等奖","三等奖","四等奖","五等奖","六等奖","七等奖","八等奖","九等奖").get(grade);
        JsonNode base=prizes.get(String.valueOf(grade));
        if(base==null||!base.canConvertToLong()||d.getPromotion()==1)return new Result("REVIEW",label+"，奖金或派奖待核实",null);
        long amount=base.longValue();
        if("DLT".equals(e.getGame())&&e.getAdditional()==1&&grade<=2) {
            JsonNode add=prizes.get("A"+grade);
            if(add==null||!add.canConvertToLong())return new Result("REVIEW",label+"，追加奖金待确认",null);
            amount=Math.addExact(amount,add.longValue());
        }
        return new Result("SETTLED",label,Math.multiplyExact(amount,e.getMultiple()));
    }
    public static BusinessException error(String message) {return new BusinessException("LOTTERY_INVALID",message);}
}
