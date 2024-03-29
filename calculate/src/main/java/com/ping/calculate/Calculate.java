package com.ping.calculate;

import java.util.Collections;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Calculate {

    public static double execute(String ariStr) throws Exception{
        int lBracket = ariStr.indexOf('(');
        if(lBracket < 0){
            return arithContainAbs(ariStr);
        }
        int rBracket = matchedRBracket(ariStr);
        String start = ariStr.substring(0, lBracket);
        String middle = ariStr.substring(lBracket + 1, rBracket);
        String end = ariStr.substring(rBracket + 1);
        return execute(start + execute(middle) + end);
    }

    public static double arithContainAbs(String str) throws Exception{
        int lAbs = str.indexOf("|");
        if(lAbs < 0){
            return simpleArith(str);
        }
        int rAbs = matchedRAbs(str);
        String start = str.substring(0, lAbs);
        String middle = str.substring(lAbs + 1, rAbs);
        String end = str.substring(rAbs + 1);
        return arithContainAbs(start + Math.abs(arithContainAbs(middle)) + end);
    }

    public static double simpleArith(String str) throws Exception{
        return arithLevel1(str);
    }

    public static double arithLevel1(String str) throws Exception{
        String[][] sl = arithSplit(str, "\\+", "\\-");
        String[] ns = sl[0];
        String[] os = sl[1];
        double rs = arithLevel2(ns[0]);
        for(int i = 1; i < ns.length; i++){
            if      ("+".equals(os[i - 1])){
                rs += arithLevel2(ns[i]);
            }else if("-".equals(os[i - 1])){
                rs -= arithLevel2(ns[i]);
            }
        }
        return rs;
    }

    public static double arithLevel2(String str) throws Exception {
        String[][] sl = arithSplit(str, "\\*", "/", "%");
        String[] ns = sl[0];
        String[] os = sl[1];
        double rs = arithLevel3(ns[0]);
        for(int i = 1; i < ns.length; i++){
            if      ("*".equals(os[i - 1])){
                rs *= arithLevel3(ns[i]);
            }else if("/".equals(os[i - 1])){
                rs /= arithLevel3(ns[i]);
            }else if("%".equals(os[i - 1])){
                rs %= arithLevel3(ns[i]);
            }
        }
        return rs;
    }

    public static double arithLevel3(String str) throws Exception{
        String[][] sl = arithSplit(str, "\\^");
        String[] ns = sl[0];
        String[] os = sl[1];
        double rs = arithLevel4(ns[0]);
        for(int i = 1; i < ns.length; i++){
            if("^".equals(os[i - 1])){
                rs = Math.pow(rs, arithLevel4(ns[i]));
            }
        }
        return rs;
    }

    public static double arithLevel4(String str) throws Exception{
        str = str.replaceAll("e", String.valueOf(Math.E)).replaceAll("π", String.valueOf(Math.PI));
        String doubleReg = "[-+]?([0-9]+(\\.[0-9]+)?|π|e)";
        double rs = 0;
        if      (str.matches("^\\|" + doubleReg + "\\|$")){//|-2.3|
            rs = Math.abs(Double.valueOf(str.substring(1, str.length() - 1)));
        }else if(str.matches("^" + doubleReg + "!$")){
            if(!str.matches("^[-+]?[0-9]+(\\.0+)?!$")){
                throw new Exception("int is needed : " + str);
            }
            int l = Integer.valueOf(str.replaceFirst("(\\.0+)?!", ""));
            rs = factorial(l);
        }else if(str.matches("^sin" + doubleReg + "°?$")){
            rs = Math.sin(getRadians(str.substring(3)));
        }else if(str.matches("^cos" + doubleReg + "°?$")){
            rs = Math.cos(getRadians(str.substring(3)));
        }else if(str.matches("^tan" + doubleReg + "°?$")){
            rs = Math.tan(getRadians(str.substring(3)));
        }else{
            rs = Double.valueOf(str);
        }
        return rs;
    }

    public static int matchedRBracket(String str) throws Exception{
        int lBracket = str.indexOf('(');
        if(lBracket < 0){
            return lBracket;
        }
        Stack<Character> stack = new Stack<Character>();
        for(int i = lBracket; i < str.length(); i++){
            char ch = str.charAt(i);
            if      (ch == '('){
                stack.push(ch);
            }else if(ch == ')'){
                stack.pop();
            }
            if(stack.isEmpty()){
                return i;
            }
        }
        throw new Exception("parse error! \"(\" and \")\" not matches");
    }

    public static int matchedRAbs(String str) throws Exception{
        if(!str.contains("|")){
            return -1;
        }
        Stack<String> stack = new Stack<String>();
        Pattern p = Pattern.compile("(?<=(^|[-+*/^])\\|{0,999999999})(\\|)|(\\|)(?=\\|{0,999999999}([-+*/^]|$))");
        Matcher m = p.matcher(str);
        while(m.find()){
            String group2 = m.group(2);
            String group3 = m.group(3);
            if      ("|".equals(group2) && group3 == null){
                stack.push(group2);
            } else {
                stack.pop();
            }
            if(stack.isEmpty()){
                return m.start();
            }
        }
        throw new Exception("parse error! \"|\" and \"|\" not matches");
    }

    public static String[][] arithSplit(String str, String... opers){
        String prefix = "(?<=([0-9]|π|e|°)[!|]?)";
        String suffix = "(?=(sin|cos|tan|\\|)?[-+]?([0-9]|π|e|°))";
        String operStr = "(";
        for(int i = 0; i < opers.length; i++){
            operStr += (i == 0 ? "" : "|") + opers[i] + (i == opers.length - 1 ? ")" : "");
        }
        String regex = prefix + operStr + suffix;
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        int start = 0;
        int end   = 0;
        StringBuilder nsStr = new StringBuilder();
        StringBuilder osStr = new StringBuilder();
        while(m.find()){
            end = m.start();
            nsStr.append(str.substring(start, end)).append(",");
            osStr.append(m.group()).append(",");
            start = m.end();
        }
        nsStr.append(str.substring(start) + ",");
        String[] ns = nsStr.toString().split(",");
        String[] os = osStr.toString().split(",");
        String[][] rs = new String[2][];
        rs[0] = ns;
        rs[1] = os;
        return rs;
    }

    public static double getRadians(String str){
        if(str.endsWith("°")){
            return Math.toRadians(Double.valueOf(str.substring(0, str.length() - 1)));
        }
        return Double.valueOf(str);
    }

    public static long factorial(int l){
        if(l == 1 || l == 0){
            return 1;
        }else if(l > 1){
            return l * factorial(l - 1);
        }else{
            return -1 * factorial(Math.abs(l));
        }
    }
}


