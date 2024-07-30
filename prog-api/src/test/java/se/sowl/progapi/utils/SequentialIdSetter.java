package se.sowl.progapi.utils;

import java.lang.reflect.Field;
import java.util.List;

public class SequentialIdSetter {

    public static <T> void setSequentialIds(List<T> entities) {
        setSequentialIds(entities, 1L); // 기본 시작 ID를 1로 설정
    }

    public static <T> void setSequentialIds(List<T> entities, long startId) {
        if (entities == null || entities.isEmpty()) {
            return;
        }

        Class<?> clazz = entities.get(0).getClass();
        Field idField;

        try {
            idField = clazz.getDeclaredField("id");
            idField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("엔티티에 ID 속성이 존재하지 않습니다. " + clazz.getName(), e);
        }

        for (int i = 0; i < entities.size(); i++) {
            try {
                idField.set(entities.get(i), startId + i);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("엔티티의 ID 인덱스 설정 도중 문제가 발생했습니다. " + i, e);
            }
        }
    }
}
