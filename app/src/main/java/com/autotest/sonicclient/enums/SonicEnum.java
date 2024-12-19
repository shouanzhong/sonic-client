/*
 *   sonic-agent  Agent of Sonic Cloud Real Machine Platform.
 *   Copyright (C) 2022 SonicCloudOrg
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.autotest.sonicclient.enums;


import java.util.stream.Stream;

/**
 * @author JayWenStar
 * @date 2022/3/13 1:55 下午
 */
public interface SonicEnum<T> {

    /**
     * 获取枚举值<T>
     *
     * @return enum value
     */
    T getValue();

    /**
     * 将value转成枚举
     */
    static <T, E extends Enum<E> & SonicEnum<T>> E valueToEnum(Class<E> enumType, T value) {
        assert enumType != null: "enum type must not be null";
        assert value != null: "value must not be null";
        assert enumType.isEnum(): "type must be an enum type";

        return Stream.of(enumType.getEnumConstants())
                .filter(item -> item.getValue().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown database value: " + value));
    }

}
