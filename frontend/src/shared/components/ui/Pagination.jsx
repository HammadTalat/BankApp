import { ChevronLeft, ChevronRight } from "lucide-react";

import Button from "./Button";

function Pagination({
    page,
    totalPages,
    onPageChange,
    disabled = false,
}) {
    if (!totalPages || totalPages <= 1) {
        return null;
    }

    const currentPage = Math.min(
        Math.max(page, 0),
        totalPages - 1,
    );
    const isFirstPage = currentPage === 0;
    const isLastPage = currentPage === totalPages - 1;

    return (
        <nav
            aria-label="Pagination"
            className="flex flex-col items-center justify-between gap-3 sm:flex-row"
        >
            <p className="text-sm font-medium text-brand-muted">
                Page {currentPage + 1} of {totalPages}
            </p>

            <div className="flex flex-wrap items-center justify-center gap-2">
                <Button
                    variant="secondary"
                    size="sm"
                    disabled={disabled || isFirstPage}
                    onClick={() => onPageChange(currentPage - 1)}
                    aria-label="Go to previous page"
                >
                    <ChevronLeft size={16} aria-hidden="true" />
                    Previous
                </Button>

                <Button
                    variant="secondary"
                    size="sm"
                    disabled={disabled || isLastPage}
                    onClick={() => onPageChange(currentPage + 1)}
                    aria-label="Go to next page"
                >
                    Next
                    <ChevronRight size={16} aria-hidden="true" />
                </Button>
            </div>
        </nav>
    );
}

export default Pagination;
