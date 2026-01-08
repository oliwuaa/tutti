package com.example.tutti.resource.folder;

import com.example.tutti.common.BaseEntity;
import com.example.tutti.orchestra.Orchestra;
import com.example.tutti.resource.instrument.InstrumentType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "folders", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"orchestra_id", "marchingType", "instrumentType", "partNumber"})
})
@Setter
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class MarchingFolder extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MarchingType marchingType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstrumentType instrumentType;

    private Integer partNumber;

    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "orchestra_id", nullable = false)
    private Orchestra orchestra;

}
