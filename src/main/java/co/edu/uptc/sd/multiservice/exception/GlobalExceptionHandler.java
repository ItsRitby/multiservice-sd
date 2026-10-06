package co.edu.uptc.sd.multiservice.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import co.edu.uptc.sd.multiservice.dto.ErrorResponseDTO;
import co.edu.uptc.sd.multiservice.exception.custom.calculator.CalculatorException;
import co.edu.uptc.sd.multiservice.exception.custom.db.DatabaseException;
import co.edu.uptc.sd.multiservice.exception.custom.db.DbPageOutOfRangeException;
import co.edu.uptc.sd.multiservice.exception.custom.db.DbPersonNotFoundException;
import co.edu.uptc.sd.multiservice.exception.custom.file.NfsException;
import co.edu.uptc.sd.multiservice.exception.custom.file.PageOutOfRangeException;
import co.edu.uptc.sd.multiservice.exception.custom.file.PersonNotFoundException;
import co.edu.uptc.sd.multiservice.util.NodeIdentifier;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final NodeIdentifier nodeIdentifier;

    public GlobalExceptionHandler(NodeIdentifier nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
    }

    @ExceptionHandler(CalculatorException.class)
    public ResponseEntity<ErrorResponseDTO> handleCalculator(CalculatorException ex) {
        return build(ex.getErrorCode(), ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return build("004",
                "Invalid numeric value for parameter '" + ex.getName() + "'.",
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDTO> handleMissing(MissingServletRequestParameterException ex) {
        return build("005",
                "Missing required parameter '" + ex.getParameterName() + "'.",
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PageOutOfRangeException.class)
    public ResponseEntity<ErrorResponseDTO> handlePageOutOfRange(PageOutOfRangeException ex) {
        return build(ex.getErrorCode(), ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PersonNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handlePersonNotFound(PersonNotFoundException ex) {
        return build(ex.getErrorCode(), ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NfsException.class)
    public ResponseEntity<ErrorResponseDTO> handleNfs(NfsException ex) {
        return build(ex.getErrorCode(), ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DbPersonNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleDbPersonNotFound(DbPersonNotFoundException ex) {
        return build(ex.getErrorCode(), ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DbPageOutOfRangeException.class)
    public ResponseEntity<ErrorResponseDTO> handleDbPageOutOfRange(DbPageOutOfRangeException ex) {
        return build(ex.getErrorCode(), ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<ErrorResponseDTO> handleDatabase(DatabaseException ex) {
        return build(ex.getErrorCode(), ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataAccess(DataAccessException ex) {
        return build("204", "Database access error.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponseDTO> build(String code, String message, HttpStatus status) {
        return ResponseEntity.status(status).body(new ErrorResponseDTO(
                code,
                message,
                nodeIdentifier.getVmHostname(),
                nodeIdentifier.getContainerName()));
    }
}
