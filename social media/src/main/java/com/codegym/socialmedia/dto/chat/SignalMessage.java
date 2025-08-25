package com.codegym.socialmedia.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignalMessage {
    private String roomId;
    private String type;
    private String sdp;
    private String candidate;
}
