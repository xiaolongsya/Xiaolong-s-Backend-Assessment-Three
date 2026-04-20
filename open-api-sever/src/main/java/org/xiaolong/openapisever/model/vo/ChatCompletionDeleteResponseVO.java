package org.xiaolong.openapisever.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatCompletionDeleteResponseVO {
    private String id;

    @Builder.Default
    private String object = "chat.completion.deleted";

    private Boolean deleted;
}
