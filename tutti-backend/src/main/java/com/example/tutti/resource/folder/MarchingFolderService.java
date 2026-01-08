package com.example.tutti.resource.folder;

import com.example.tutti.resource.instrument.InstrumentType;
import java.util.List;

public interface MarchingFolderService {
    MarchingFolderResponse addFolder(Long orchestraId, MarchingFolderRequest folderRequest);
    void deleteFolder(Long folderId);
    MarchingFolderResponse incrementQuantity(Long folderId);
    MarchingFolderResponse decrementQuantity(Long folderId);
    List<MarchingFolderResponse> getStats(Long orchestraId, MarchingType marchingType, InstrumentType instrumentType, Integer part);
}