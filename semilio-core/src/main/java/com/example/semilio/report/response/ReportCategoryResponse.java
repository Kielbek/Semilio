package com.example.semilio.report.response;

import java.util.List;

public record ReportCategoryResponse(
        String id,
        String label,
        String description,
        List<SubcategoryResponse> subcategories
) {}