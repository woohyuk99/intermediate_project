package com.deokhugam.backend.repository.query;

import com.deokhugam.backend.dto.cursor.CursorPageResponsePopularBookDto;
import com.deokhugam.backend.dto.dashboard.PopularBookQuery;

public interface PopularBookQueryRepository {
    CursorPageResponsePopularBookDto findByPopularBookIdWithCursor(PopularBookQuery query);
}
