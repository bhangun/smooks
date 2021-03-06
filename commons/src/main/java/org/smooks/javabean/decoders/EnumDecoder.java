/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.javabean.decoders;

import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.config.Configurable;
import org.smooks.javabean.DataDecodeException;
import org.smooks.javabean.DataDecoder;
import org.smooks.javabean.DecodeType;
import org.smooks.util.ClassUtil;

import java.util.Properties;

/**
 * Enum instance decoder.
 * <p/>
 * The enumeration type is specified through the "<b>enumType</b>" configuration
 * param. Enum constant value mappings can be performed as per the
 * {@link org.smooks.javabean.decoders.MappingDecoder}.
 * The "<b>strict</b>" configuration param determines how data that do not
 * map to valid enum constants will be handled.  Under the default behavior, or
 * when specifying strict as "true", an error will be thrown.  If strict is
 * "false" null will be returned
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@DecodeType({Enum.class})
public class EnumDecoder implements DataDecoder, Configurable {

    private Properties configuration;
    private Class enumType;
    private MappingDecoder mappingDecoder = new MappingDecoder();
    private boolean strict = true;

    public void setConfiguration(Properties resourceConfig) throws SmooksConfigurationException {
        String enumTypeName = resourceConfig.getProperty("enumType");

        if(enumTypeName == null || enumTypeName.trim().equals(""))
        {
            throw new SmooksConfigurationException("Invalid EnumDecoder configuration. 'enumType' param not specified.");
        }

        try {
            enumType = ClassUtil.forName(enumTypeName.trim(), EnumDecoder.class);
        } catch (ClassNotFoundException e) {
            throw new SmooksConfigurationException("Invalid Enum decoder configuration.  Failed to resolve '" + enumTypeName + "' as a Java Enum Class.", e);
        }

        if(!Enum.class.isAssignableFrom(enumType)) {
            throw new SmooksConfigurationException("Invalid Enum decoder configuration.  Resolved 'enumType' '" + enumTypeName + "' is not a Java Enum Class.");
        }
        strict = resourceConfig.getProperty("strict", "true").equals("true");

        mappingDecoder.setConfiguration(resourceConfig);
        mappingDecoder.setStrict(false);

        this.configuration = resourceConfig;
    }

    public Properties getConfiguration() {
        return configuration;
    }

    public void setEnumType(Class enumType) {
        this.enumType = enumType;
    }

    @SuppressWarnings("unchecked")
    public Object decode(String data) throws DataDecodeException {
        String mappedValue = (String) mappingDecoder.decode(data);

        try {
            return Enum.valueOf(enumType, mappedValue.trim());
        } catch(IllegalArgumentException e) {
            if(strict) {
                throw new DataDecodeException("Failed to decode '" + mappedValue + "' as a valid Enum constant of type '" + enumType.getName() + "'.");
            } else {
                return null;
            }

        }
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }
}
