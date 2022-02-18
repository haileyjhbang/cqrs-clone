package com.cqrs.query.version;

import com.cqrs.event.HolderCreationEvent;
import org.axonframework.serialization.SimpleSerializedType;
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation;
import org.axonframework.serialization.upcasting.event.SingleEventUpcaster;
import org.dom4j.Document;

public class HolderCreationEventV1 extends SingleEventUpcaster {
    //대상 이벤트 지정
    //최초에는 revision 정보 명시하지 않았기에 null로 지정
    private static SimpleSerializedType targetType = new SimpleSerializedType(HolderCreationEvent.class.getTypeName(), null);

    @Override
    protected boolean canUpcast(IntermediateEventRepresentation intermediateRepresentation) {
        return intermediateRepresentation.getType().equals(targetType);
    }

    /**
     * 실제 Event Version을 확인하고 이전 버전의 Event가 들어왔을 때 행동해야 할 내용을 기술
     *
     * EventStore에 저장된 Event 내용이 XML로 지정되므로, XML로 되어있는 Payload 에서 신규 추가된 company 정보와 값이 없을 경우 입력될 Default 값 N/A를 setText 메소드를 통해 지정
     * 해당 작업을 수행할 대상을 Revision 정보가 null인 targetType으로 한정
     * 즉, HolderCreationEvent에 많은 Revision이 존재하더라도, HolderEventCreationEventV1 클래스에서는 Revision 번호가 1.0과 null인 두 Event간의 속성값에만 영향을 미침
     * @param intermediateRepresentation
     * @return
     */
    @Override
    protected IntermediateEventRepresentation doUpcast(IntermediateEventRepresentation intermediateRepresentation) {
        return intermediateRepresentation.upcastPayload(
                new SimpleSerializedType(targetType.getName(), "1.0"),
                Document.class,
                document -> {
                    document.getRootElement()
                        .addElement("company")
                            .setText("N/A");
                    return document;
                }
        );
    }
}
