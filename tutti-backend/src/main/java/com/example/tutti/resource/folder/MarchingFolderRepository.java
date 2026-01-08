package com.example.tutti.resource.folder;

import com.example.tutti.resource.instrument.InstrumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MarchingFolderRepository extends JpaRepository<MarchingFolder, Long> {
    Optional<MarchingFolder> findByOrchestraIdAndMarchingTypeAndInstrumentTypeAndPartNumber(
            Long orchestraId,
            MarchingType marchingType,
            InstrumentType instrumentType,
            Integer partNumber
    );

    @Query("SELECT f FROM MarchingFolder f WHERE f.orchestra.id = :orchId " +
            "AND (:mType IS NULL OR f.marchingType = :mType) " +
            "AND (:iType IS NULL OR f.instrumentType = :iType) " +
            "AND (:part IS NULL OR f.partNumber = :part)")
    List<MarchingFolder> findFoldersFiltered(
            @Param("orchId") Long orchestraId,
            @Param("mType") MarchingType marchingType,
            @Param("iType") InstrumentType instrumentType,
            @Param("part") Integer partNumber
    );
}