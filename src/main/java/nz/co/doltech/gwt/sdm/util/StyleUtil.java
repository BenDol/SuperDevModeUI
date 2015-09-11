/**
 * Copyright 2015 Doltech Systems Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package nz.co.doltech.gwt.sdm.util;

import com.google.gwt.dom.client.Style.Unit;

public class StyleUtil {

    public static Double getMeasurementValue(String value) {
        if(value == null) {
            return null;
        }
        return Double.parseDouble(value
            .replaceAll(Unit.CM.getType(), "")
            .replaceAll(Unit.EM.getType(), "")
            .replaceAll(Unit.EX.getType(), "")
            .replaceAll(Unit.IN.getType(), "")
            .replaceAll(Unit.MM.getType(), "")
            .replaceAll(Unit.PC.getType(), "")
            .replaceAll(Unit.PCT.getType(), "")
            .replaceAll(Unit.PT.getType(), "")
            .replaceAll(Unit.PX.getType(), ""));
    }

    public static Unit getMeasurementUnit(String value) {
        if(value == null) { return null; }
        try {
            return Unit.valueOf(value.replaceAll("^[\\d-]*\\s*", "").toUpperCase());
        }
        catch(IllegalArgumentException e) {
            // Silently catch invalid units
            return null;
        }
    }
}
