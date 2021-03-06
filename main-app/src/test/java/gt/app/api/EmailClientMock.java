package gt.app.api;

import gt.api.email.EmailDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Component
public class EmailClientMock implements EmailClient {

    @Override
    public ResponseEntity<Void> sendEmailWithAttachments(@Valid @NotNull EmailDto email) {
        return ResponseEntity.noContent().build();
    }
}
