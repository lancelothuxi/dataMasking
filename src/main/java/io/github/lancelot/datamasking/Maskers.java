package io.github.lancelot.datamasking;

import org.apache.commons.lang.StringUtils;

/**
 * 内置脱敏规则。
 *
 * <p>每种 {@link MaskType} 对应这里的一条静态规则；入口为
 * {@link #mask(MaskType, String)}。规则方法均为纯函数：同一输入必得同一输出。</p>
 *
 * @author lancelot
 */
public final class Maskers {

    private static final String NULLSTR = "null";

    private Maskers() {
    }

    /**
     * [中文姓名] 只显示第一个汉字，其他隐藏为星号，如 李四 → 李*。
     */
    public static String chineseName(String fullName) {
        if (StringUtils.isBlank(fullName)) {
            return fullName;
        }
        String name = StringUtils.left(fullName, 1);
        return StringUtils.rightPad(name, 2, "*");
    }

    /**
     * [证件号码类]（身份证，军官证，护照等身份证明证件类）前 3 位显示，后 3 位显示，
     * 中间部分用 ******（6 个 *）代替；不足 8 位直接 ******（6 个 *）代替。
     */
    public static String idCardNum(String id) {
        if (StringUtils.isBlank(id)) {
            return id;
        }
        if (id.length() > 8) {
            return StringUtils.left(id, 3) + "******" + StringUtils.right(id, 3);
        }
        return "******";
    }

    /**
     * [固定电话] 后四位显示，其他隐藏，如 01012345678 → *******5678。
     */
    public static String fixedPhone(String phone) {
        if (StringUtils.isBlank(phone)) {
            return phone;
        }
        if (phone.length() > 4) {
            return StringUtils.leftPad(StringUtils.right(phone, 4),
                    StringUtils.length(phone), "*");
        }
        return "******";
    }

    /**
     * [手机号码类]（联系人电话，个人手机）前 3 位显示，后 4 位显示，
     * 中间部分 ******（6 个 *）代替；不足 7 位直接 ******（6 个 *）代替。
     */
    public static String mobilePhone(String phone) {
        if (StringUtils.isBlank(phone)) {
            return phone;
        }
        if (phone.length() > 7) {
            return StringUtils.left(phone, 3) + "******" + StringUtils.right(phone, 4);
        }
        return "******";
    }

    /**
     * [地址类] 前 2 位显示，后 2 位显示，中间部分 ******（6 个 *）代替；不足 4 位全部 ****** 代替。
     */
    public static String address(String address) {
        if (StringUtils.isBlank(address)) {
            return address;
        }
        if (address.length() > 4) {
            return StringUtils.left(address, 2) + "******" + StringUtils.right(address, 2);
        }
        return "******";
    }

    /**
     * [邮箱类] 最后一个点（.）之前全部 ******（6 个 *）代替，点之后显示，如 test@example.com → ******.com。
     */
    public static String email(String email) {
        if (StringUtils.isBlank(email)) {
            return email;
        }
        int index = email.lastIndexOf(".");
        if (index > 1) {
            return "******" + StringUtils.right(email, email.length() - index);
        }
        return "******";
    }

    /**
     * [卡号] 前 2 位显示，后 4 位显示，中间部分 ******（6 个 *）代替。
     */
    public static String bankCard(String cardNum) {
        if (StringUtils.isBlank(cardNum)) {
            return cardNum;
        }
        if (cardNum.length() > 6) {
            return StringUtils.left(cardNum, 2) + "******" + StringUtils.right(cardNum, 4);
        }
        return "******";
    }

    /**
     * [银行名] 显示前 4 位，其余隐藏为星号（不足 4 位只显示第一位）。
     */
    public static String bankName(String bankName) {
        if (StringUtils.isBlank(bankName)) {
            return bankName;
        }
        if (bankName.length() > 4) {
            return StringUtils.rightPad(StringUtils.left(bankName, 4),
                    StringUtils.length(bankName), "*");
        }
        return StringUtils.rightPad(StringUtils.left(bankName, 1),
                StringUtils.length(bankName), "*");
    }

    /**
     * [统一社会信用代码类（注册码，营业执照，牌照类的码）] 前 4 位显示，后 4 位显示，
     * 中间部分 ******（6 个 *）代替。
     */
    public static String cnapsCode(String code) {
        if (StringUtils.isBlank(code)) {
            return code;
        }
        if (code.length() > 8) {
            return StringUtils.left(code, 4) + "******" + StringUtils.right(code, 4);
        }
        return "******";
    }

    /**
     * [银行卡有效期] 前 1 位，后 1 位，其他隐藏，如 0126 → 0**6。
     */
    public static String cardValidDate(String date) {
        if (StringUtils.isBlank(date)) {
            return date;
        }
        return StringUtils.left(date, 1).concat(
                StringUtils.removeStart(StringUtils.leftPad(
                        StringUtils.right(date, 1), StringUtils.length(date),
                        "*"), "*"));
    }

    /**
     * 全部隐藏为指定数量的星号。
     */
    public static String all(String data, int sensitiveSize) {
        return StringUtils.repeat("*", sensitiveSize);
    }

    /**
     * 按类型执行脱敏。
     *
     * <p>约定：空值返回空串；字符串 "null" 保持原样返回，便于排查数据问题。</p>
     *
     * @param type 信息类型
     * @param value 字段值
     * @return 脱敏后的值
     */
    public static String mask(MaskType type, String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        // 返回 null 时保持原样返回方便查问题
        if (NULLSTR.equalsIgnoreCase(value)) {
            return value;
        }
        switch (type) {
            case CHINESE_NAME: {
                value = Maskers.chineseName(value);
                break;
            }
            case ID_CARD: {
                value = Maskers.idCardNum(value);
                break;
            }
            case FIXED_PHONE: {
                value = Maskers.fixedPhone(value);
                break;
            }
            case MOBILE_PHONE: {
                value = Maskers.mobilePhone(value);
                break;
            }
            case ADDRESS: {
                value = Maskers.address(value);
                break;
            }
            case EMAIL: {
                value = Maskers.email(value);
                break;
            }
            case BANK_CARD: {
                value = Maskers.bankCard(value);
                break;
            }
            case BANK_NAME: {
                value = Maskers.bankName(value);
                break;
            }
            case CNAPS_CODE: {
                value = Maskers.cnapsCode(value);
                break;
            }
            case BANK_CARD_DATE: {
                value = Maskers.cardValidDate(value);
                break;
            }
            case ALL: {
                value = Maskers.all(value, 3);
                break;
            }
            case NULL: {
                value = "";
                break;
            }
        }
        return value;
    }
}