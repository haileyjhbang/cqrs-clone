package com.cqrs.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public class HolderCreationEvent {
    private String holderID;
    private String holderName;
    private String tel;
    private String address;
}