import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import PaginationControls from './PaginationControls.jsx';

describe('PaginationControls', () => {
  it('disables Previous on the first page and enables Next', () => {
    render(
      <PaginationControls
        pageInfo={{ number: 0, totalPages: 3, totalElements: 25 }}
        loading={false}
        onPageChange={vi.fn()}
      />
    );

    expect(screen.getByText('Previous')).toBeDisabled();
    expect(screen.getByText('Next')).toBeEnabled();
    expect(screen.getByText('Page 1 of 3 (25 total)')).toBeInTheDocument();
  });

  it('disables Next on the last page and enables Previous', () => {
    render(
      <PaginationControls
        pageInfo={{ number: 2, totalPages: 3, totalElements: 25 }}
        loading={false}
        onPageChange={vi.fn()}
      />
    );

    expect(screen.getByText('Previous')).toBeEnabled();
    expect(screen.getByText('Next')).toBeDisabled();
  });

  it('disables both buttons while loading, even mid-list', () => {
    render(
      <PaginationControls
        pageInfo={{ number: 1, totalPages: 3, totalElements: 25 }}
        loading
        onPageChange={vi.fn()}
      />
    );

    expect(screen.getByText('Previous')).toBeDisabled();
    expect(screen.getByText('Next')).toBeDisabled();
  });

  it('calls onPageChange with the previous/next page index', () => {
    const onPageChange = vi.fn();
    render(
      <PaginationControls
        pageInfo={{ number: 1, totalPages: 3, totalElements: 25 }}
        loading={false}
        onPageChange={onPageChange}
      />
    );

    fireEvent.click(screen.getByText('Next'));
    expect(onPageChange).toHaveBeenCalledWith(2);

    fireEvent.click(screen.getByText('Previous'));
    expect(onPageChange).toHaveBeenCalledWith(0);
  });

  it('treats zero total pages as both first and last page', () => {
    render(
      <PaginationControls pageInfo={{ number: 0, totalPages: 0, totalElements: 0 }} loading={false} onPageChange={vi.fn()} />
    );

    expect(screen.getByText('Previous')).toBeDisabled();
    expect(screen.getByText('Next')).toBeDisabled();
    expect(screen.getByText('Page 0 of 0 (0 total)')).toBeInTheDocument();
  });
});
