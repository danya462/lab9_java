package ru.university.lab9.model;

public enum ProtectionLevel {
    LEVEL_0("0 - direct plain TCP", false, false),
    LEVEL_1("1 - proxy", true, false),
    LEVEL_2("2 - ProtoBuf tunnel", false, true),
    LEVEL_3("3 - proxy + ProtoBuf", true, true);

    private final String title;
    private final boolean useProxy;
    private final boolean useProto;

    ProtectionLevel(String title, boolean useProxy, boolean useProto) {
        this.title = title;
        this.useProxy = useProxy;
        this.useProto = useProto;
    }

    public boolean useProxy() {
        return useProxy;
    }

    public boolean useProto() {
        return useProto;
    }

    @Override
    public String toString() {
        return title;
    }
}
