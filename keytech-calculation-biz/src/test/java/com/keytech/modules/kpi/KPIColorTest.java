package com.keytech.modules.kpi;

import cn.hutool.core.math.Calculator;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @FileName KPIColorTest
 * @Description
 * @Author HQ
 * @date 2025-07-23
 **/
public class KPIColorTest {

    /**
     * 计算颜色(比率)
     * 1:2:3:4-> 4/3 3/2 2/1
     * 目标比实际值大的数据就不合格
     * @param targetValue
     * @param actualValue
     * @return
     */
    private static int doColour(String targetValue, String actualValue) {
        Map<Integer, String> targetMap = getRatioMap(targetValue);
        Map<Integer, String> nowMap = getRatioMap(actualValue);
        //颜色
        int colour = 0; //无色
        //实际值为 1. 0:0:0:0(不达标)  2.分子非0 分母为0 无效(不达标)
        if(!notStandard(actualValue)){
            return 2;
        }

        //如果实际的都是0/0 那么就是合格的
        boolean match = nowMap.values().stream().allMatch(v -> Objects.equals("0/0", v));
        if (match) {
            //比率都为零(达标)
            colour = 1; //绿色
            //colour = 2; //红色
        }
        for (Map.Entry<Integer, String> integerStringEntry : targetMap.entrySet()) {

            Integer key = integerStringEntry.getKey();
            String value = integerStringEntry.getValue();
            //实际
            String nowValue = nowMap.get(key);
            //目标判断
//            if ((value != null && value.endsWith("/0")) || StrUtil.isEmpty(value) || StrUtil.isEmpty(nowValue)) {
//                continue;
//            }
            if (Objects.equals(nowValue, "0/0") || Objects.equals(value, "0/0") ||
                    StrUtil.isEmpty(value) || StrUtil.isEmpty(nowValue)) {
                continue;
            }
//            if (Objects.equals(value, "0/0") ||
//                    StrUtil.isEmpty(value) || StrUtil.isEmpty(nowValue)) {
//                continue;
//            }
            //实际值判断 (为0不合格)
//            if (Objects.equals(nowValue, "0/0")) {
//                //不合格 红色
//                colour = 2;
//                break;
//            }

            //计算
            double conversionT = Calculator.conversion(value);
            double conversionN = Calculator.conversion(nowValue);
            //存在目标比实际值大的数据就不合格 1:green ,2:red
            if (NumberUtil.compare(conversionT, conversionN) > 0) {
                //不合格 红色
                colour = 2;
                break;
            } else {
                //合格绿色
                colour = 1;
            }

        }
        return colour;
    }



    /**
     * 比例值的替换
     *
     * @param value
     * @return
     */
    private static Map<Integer, String> getRatioMap(String value) {
        int count = StrUtil.count(value, ":");
        List<String> split = StrUtil.split(value, ":");
        int num = 0;
        Map<Integer, String> idxMap = new HashMap<>();
        for (int i = split.size() - 1; i >= 0; i--) {
            if (num == count) {
                break;
            }
            int preIdx = i - 1;
            if (preIdx < 0) {
                break;
            }
            //当前值
            String cur = split.get(i);
            //上个值
            String pre = split.get(preIdx);
            //处理0
            if (NumberUtil.isNumber(cur) && NumberUtil.parseDouble(cur) == 0 ||
                    NumberUtil.isNumber(pre) && NumberUtil.parseDouble(pre) == 0) {
                cur = "0";
                pre = "0";
            }
            idxMap.put(num++, cur + "/" + pre);
        }
        return idxMap;
    }

    /**
     * 不达标判断
     *  actualValue 实际值
     *  实际值为 1. 0:0:0:0(不达标)  2.分子非0 分母为0 无效(不达标)
     */
    public static boolean notStandard(String actualValue){
        if("0:0:0:0".equals(actualValue)){
            return false;
        }
        String[] parts = actualValue.split(":");
        if (parts.length < 2) {
            return false; // 至少需要两个数字才能形成比率
        }
        for (int i = 0; i < parts.length - 1; i++) {
            try {
                int numerator = Integer.parseInt(parts[i].trim());
                int denominator = Integer.parseInt(parts[i + 1].trim());
                // 检查比率有效性：分子非0且分母为0时无效
                if (numerator != 0 && denominator == 0) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false; // 数字格式错误
            }
        }
        return true;
    }

    @Test
    public void testColor(){
        //目标  实际
        System.out.println("0:0:2:14=="+doColour("1:5:60:300","0:0:3:19"));
    }



    //colour 0 没颜色 1:green ,2:red
    public static void main(String[] args) {
        //目标  实际
//        System.out.println("=="+doColour("1:5:60:300","0:1:14:80"));

//        System.out.println("0:1:14:80=="+doColour("1:5:60:300","0:1:14:80"));
//        System.out.println("5:27:330:1680=="+doColour("1:5:60:300","5:27:330:1680"));
//        System.out.println("0:0:1:126=="+doColour("1:5:60:300","0:0:1:126"));
//        System.out.println("0:0:7:113=="+doColour("1:5:60:300","0:0:7:113"));
//        System.out.println("0:0:0:284=="+doColour("1:5:60:300","0:0:0:284"));
//        System.out.println("0:0:0:53=="+doColour("1:5:60:300","0:0:0:53"));
//        System.out.println("0:2:13:372=="+doColour("1:5:60:300","0:2:13:372"));
//        System.out.println("0:3:14:598=="+doColour("1:5:60:300","0:3:14:598"));
//        System.out.println("0:10:0:78=="+doColour("1:5:60:300","0:10:0:78"));
//        System.out.println("4:99:3:523=="+doColour("1:5:60:300","4:99:3:523"));
//        System.out.println("1:13:29:831=="+doColour("1:5:60:300","1:13:29:831"));
//        System.out.println("1:0:6:214=="+doColour("1:5:60:300","1:0:6:214"));
//        System.out.println("1:0:0:202=="+doColour("1:5:60:300","1:0:0:202"));
//        System.out.println("1:0:0:92=="+doColour("1:5:60:300","1:0:0:92"));
//        System.out.println("0:0:0:0=="+doColour("1:5:60:300","0:0:0:0"));
//        System.out.println("1:1:1:0=="+doColour("1:5:60:300","1:1:1:0"));
//        System.out.println("0:0:51:1428=="+doColour("1:5:60:300","0:0:51:1428"));
//        System.out.println("1:1:1:1=="+doColour("1:5:60:300","0:0:51:1428"));
        System.out.println("0:0:2:14=="+doColour("1:5:60:300","0:0:3:19"));
    }



}
