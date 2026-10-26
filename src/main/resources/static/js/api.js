// ===================================================================
// Shared API Client & Helper Utilities (Vanilla JS)
// ===================================================================

const API_BASE = '/api';

/**
 * Returns a persistent cartKey for the browser session.
 */
function getCartKey() {
    let key = localStorage.getItem('ecom_cart_key');
    if (!key) {
        key = 'cart-' + Math.random().toString(36).substring(2, 10);
        localStorage.setItem('ecom_cart_key', key);
    }
    return key;
}

/**
 * Formats a numeric value as standard USD currency.
 */
function formatCurrency(amount) {
    const val = parseFloat(amount);
    return isNaN(val) ? '$0.00' : `$${val.toFixed(2)}`;
}

/**
 * Parses and returns the error message from an API ErrorResponse or Error object.
 */
async function parseErrorMessage(response) {
    try {
        const data = await response.json();
        if (data.message) return data.message;
        if (data.error) return data.error;
    } catch {
        // Response is not JSON
    }
    return `Server returned HTTP ${response.status} (${response.statusText})`;
}

/**
 * Fetch wrapper for GET requests.
 */
async function apiGet(endpoint) {
    const response = await fetch(`${API_BASE}${endpoint}`);
    if (!response.ok) {
        const msg = await parseErrorMessage(response);
        throw new Error(msg);
    }
    return await response.json();
}

/**
 * Fetch wrapper for POST requests.
 */
async function apiPost(endpoint, body) {
    const response = await fetch(`${API_BASE}${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: body ? JSON.stringify(body) : null
    });
    if (!response.ok) {
        const msg = await parseErrorMessage(response);
        throw new Error(msg);
    }
    // Return json if content exists, otherwise empty object
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
        return await response.json();
    }
    return {};
}

/**
 * Fetch wrapper for PATCH requests.
 */
async function apiPatch(endpoint, body) {
    const response = await fetch(`${API_BASE}${endpoint}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
    if (!response.ok) {
        const msg = await parseErrorMessage(response);
        throw new Error(msg);
    }
    return await response.json();
}

/**
 * Fetch wrapper for DELETE requests.
 */
async function apiDelete(endpoint) {
    const response = await fetch(`${API_BASE}${endpoint}`, {
        method: 'DELETE'
    });
    if (!response.ok) {
        const msg = await parseErrorMessage(response);
        throw new Error(msg);
    }
    if (response.status === 204) return {};
    try {
        return await response.json();
    } catch {
        return {};
    }
}

/**
 * Displays user notification in the page's message banner.
 */
function showMessage(text, type = 'error') {
    const banner = document.getElementById('message-banner');
    const textSpan = document.getElementById('message-text');
    if (!banner || !textSpan) return;

    banner.className = `message-banner ${type}`;
    textSpan.textContent = text;
    banner.style.display = 'flex';

    if (type === 'success' || type === 'info') {
        setTimeout(clearMessage, 4000);
    }
}

/**
 * Clears and hides the message banner.
 */
function clearMessage() {
    const banner = document.getElementById('message-banner');
    if (banner) {
        banner.style.display = 'none';
    }
}

/**
 * Updates the cart badge count in the header.
 */
async function updateCartBadge() {
    const badge = document.getElementById('cart-count-badge');
    if (!badge) return;
    try {
        const cartKey = getCartKey();
        const cart = await apiGet(`/cart?cartKey=${encodeURIComponent(cartKey)}`);
        const count = (cart.items || []).reduce((acc, item) => acc + item.quantity, 0);
        badge.textContent = count;
    } catch (err) {
        console.warn('Could not update cart count badge:', err.message);
    }
}

// Automatically update cart badge whenever page loads
document.addEventListener('DOMContentLoaded', () => {
    updateCartBadge();
});
