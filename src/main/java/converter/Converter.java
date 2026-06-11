package converter;

public interface Converter<S, T> {
    S convert(T type);
}
