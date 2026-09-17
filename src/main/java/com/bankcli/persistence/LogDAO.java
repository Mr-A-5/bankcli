package com.bankcli.persistence;

import com.bankcli.domain.Log;
import java.util.List;

public interface LogDAO {
	void writeLog(Log logs);
	List<Log> getAllLogs();
}
