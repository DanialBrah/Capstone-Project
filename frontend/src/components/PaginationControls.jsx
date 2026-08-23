export default function PaginationControls({ pageInfo, loading, onPageChange }) {
  const currentPage = pageInfo.number;
  const totalPages = pageInfo.totalPages;
  const isFirstPage = currentPage <= 0;
  const isLastPage = totalPages === 0 || currentPage >= totalPages - 1;

  return (
    <div className="pagination-controls">
      <button
        type="button"
        disabled={loading || isFirstPage}
        onClick={() => onPageChange(currentPage - 1)}
      >
        Previous
      </button>
      <span>
        Page {totalPages === 0 ? 0 : currentPage + 1} of {totalPages} ({pageInfo.totalElements} total)
      </span>
      <button
        type="button"
        disabled={loading || isLastPage}
        onClick={() => onPageChange(currentPage + 1)}
      >
        Next
      </button>
    </div>
  );
}
