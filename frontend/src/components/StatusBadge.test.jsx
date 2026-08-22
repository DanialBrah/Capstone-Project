import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import StatusBadge from './StatusBadge.jsx';

describe('StatusBadge', () => {
  it('renders the status text and a lowercased status class', () => {
    render(<StatusBadge status="ACTIVE" />);

    const badge = screen.getByText('ACTIVE');
    expect(badge).toBeInTheDocument();
    expect(badge).toHaveClass('status-badge', 'status-active');
  });

  it('lowercases mixed-case statuses for the CSS class', () => {
    render(<StatusBadge status="Cancelled" />);

    expect(screen.getByText('Cancelled')).toHaveClass('status-cancelled');
  });
});
