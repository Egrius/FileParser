package by.egrius.app.mapper;

public interface BaseMapper <F, T>{
    T map(F object);

    default T map(F fromObject, T toObject) {
        return toObject;
    }
}
