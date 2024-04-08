package net.study.util;

public class MessagesPaginationUtil {
    private static int page = 0;
    public static void setPage(int page) {
        MessagesPaginationUtil.page = page;
    }
    public static int getCurrentPage() {
        return MessagesPaginationUtil.page;
    }
    public static void incrementOnValue(int value) {
         MessagesPaginationUtil.page += value;
    }
    public static void reset() {
        MessagesPaginationUtil.page = 0;
    }
}
