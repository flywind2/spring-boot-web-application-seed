package gt.app.service;

import gt.app.domain.Note;
import gt.app.domain.ReceivedFile;
import gt.app.dto.note.NoteCreateDto;
import gt.app.dto.note.NoteEditDto;
import gt.app.dto.note.NoteReadDto;
import gt.app.mapper.NoteMapper;
import gt.app.repository.NoteRepository;
import gt.app.service.file.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@Slf4j
@RequiredArgsConstructor
public class NoteService {

    private static final ReceivedFile.FileGroup FILE_GROUP = ReceivedFile.FileGroup.NOTE_ATTACHMENT;
    private final NoteRepository noteRepository;
    private final FileService fileService;


    public Note createNote(NoteCreateDto dto) {

        List<ReceivedFile> files = new ArrayList<>();
        for (MultipartFile mpf : dto.getFiles()) {

            if (mpf.isEmpty()) {
                continue;
            }

            String fileId = fileService.store(FILE_GROUP, mpf);
            files.add(new ReceivedFile(FILE_GROUP, mpf.getOriginalFilename(), fileId));
        }

        Note note = NoteMapper.INSTANCE.userToUserDto(dto);
        note.getAttachedFiles().addAll(files);

        return noteRepository.save(note);
    }

    public Note update(NoteEditDto dto) {

        Optional<Note> noteOpt = noteRepository.findById(dto.getId());
        return noteOpt.map(note -> {
                NoteMapper.INSTANCE.userToUserDto(dto, note);
                return noteRepository.save(note);
            }
        ).orElseThrow();
    }

    public NoteReadDto read(Long id) {
        return noteRepository.findById(id)
            .map(NoteMapper.INSTANCE::mapForRead)
            .orElseThrow();
    }

    public Page<NoteReadDto> readAll(Pageable pageable) {
        return noteRepository.findAll(pageable)
            .map(NoteMapper.INSTANCE::mapForRead);
    }

    public Page<NoteReadDto> readAllByUser(Pageable pageable, Long userId) {
        return noteRepository.findByCreatedByUser_IdOrderByCreatedDateDesc(pageable, userId)
            .map(NoteMapper.INSTANCE::mapForRead);
    }

    public void delete(Long id) {
        noteRepository.deleteById(id);
    }

}
