/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
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
 */
package dev.ikm.komet.amplify.lidr.events;

import dev.ikm.komet.framework.events.Evt;
import dev.ikm.komet.framework.events.EvtType;

public class AddResultEvent extends Evt {

    public static final EvtType<AddResultEvent> ADD_RESULT_TO_ANALYTE_GROUP = new EvtType<>(Evt.ANY, "ADD_RESULT_TO_ANALYTE_GROUP");

    private final Object oneResult;
    /**
     * Constructs a prototypical Event.
     *
     * @param source         the object on which the Event initially occurred
     * @param eventType
     */
    public AddResultEvent(Object source, EvtType eventType, Object oneResult) {
        super(source, eventType);
        this.oneResult = oneResult;
    }

    public Object getOneResult() {
        return oneResult;
    }
}
