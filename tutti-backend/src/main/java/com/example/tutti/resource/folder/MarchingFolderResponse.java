package com.example.tutti.resource.folder;

import com.example.tutti.resource.instrument.InstrumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarchingFolderResponse {

    private Long id;
    private MarchingType marchingType;
    private InstrumentType instrumentType;
    private Integer partNumber;
    private Long orchestraId;
    private Integer quantity;
}
