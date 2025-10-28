package by.egrius.app.repository;

import by.egrius.app.entity.FileEventLog;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FileEventLogRepository extends CrudRepository<FileEventLog, UUID> {

}