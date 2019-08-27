package lex.common.dynamic.routing.key;

public interface KeyHolder {

    String getKey();

    void setKey(String key, boolean usePrime);

    void removeKey(String key);
}
