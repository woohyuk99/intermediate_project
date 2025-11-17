package com.deokhugam.backend.repository.query;

import com.deokhugam.backend.dto.cursor.CursorPageResponsePowerUserDto;
import com.deokhugam.backend.dto.dashboard.PowerUserQuery;

public interface PowerUserQueryRepository {
    CursorPageResponsePowerUserDto findByPowerUserIdWithCursor(PowerUserQuery query);
}
