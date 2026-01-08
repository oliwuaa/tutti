package com.example.tutti.resource.folder;

import com.example.tutti.exception.NotFoundException;
import com.example.tutti.orchestra.Orchestra;
import com.example.tutti.orchestra.OrchestraRepository;
import com.example.tutti.resource.instrument.InstrumentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MarchingFolderServiceImpl implements MarchingFolderService {

    private final MarchingFolderRepository folderRepository;
    private final OrchestraRepository orchestraRepository;
    private final MarchingFolderMapper folderMapper;

    @Override
    public MarchingFolderResponse addFolder(Long orchestraId, MarchingFolderRequest request) {
        Orchestra orchestra = orchestraRepository.findById(orchestraId)
                .orElseThrow(() -> new NotFoundException("Orkiestra nie istnieje"));

        MarchingFolder folder = folderRepository.findByOrchestraIdAndMarchingTypeAndInstrumentTypeAndPartNumber(
                orchestraId, request.getMarchingType(), request.getInstrumentType(), request.getPartNumber()
        ).map(existing -> {
            existing.setQuantity(existing.getQuantity() + 1);
            return existing;
        }).orElseGet(() -> MarchingFolder.builder()
                .marchingType(request.getMarchingType())
                .instrumentType(request.getInstrumentType())
                .partNumber(request.getPartNumber())
                .orchestra(orchestra)
                .quantity(1)
                .build());

        return folderMapper.toResponse(folderRepository.save(folder));
    }

    @Override
    public void deleteFolder(Long folderId) {
        if (!folderRepository.existsById(folderId)) {
            throw new NotFoundException("Folder nie istnieje");
        }
        folderRepository.deleteById(folderId);
    }

    @Override
    public MarchingFolderResponse incrementQuantity(Long folderId) {
        MarchingFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new NotFoundException("Folder nie istnieje"));

        if (folder.getQuantity() >= 999) {
            throw new IllegalArgumentException("Osiągnięto limit pojemności folderu");
        }

        folder.setQuantity(folder.getQuantity() + 1);
        return folderMapper.toResponse(folderRepository.save(folder));
    }

    @Override
    public MarchingFolderResponse decrementQuantity(Long folderId) {
        MarchingFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new NotFoundException("Folder nie istnieje"));

        if (folder.getQuantity() <= 0) {
            throw new IllegalArgumentException("Ilość wynosi już 0.");
        }

        folder.setQuantity(folder.getQuantity() - 1);

        if (folder.getQuantity() == 0) {
            folderRepository.delete(folder);
            return null;
        }

        return folderMapper.toResponse(folderRepository.save(folder));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarchingFolderResponse> getStats(Long orchestraId, MarchingType marchingType, InstrumentType instrumentType, Integer part) {
        return folderRepository.findFoldersFiltered(orchestraId, marchingType, instrumentType, part)
                .stream()
                .map(folderMapper::toResponse)
                .toList();
    }
}