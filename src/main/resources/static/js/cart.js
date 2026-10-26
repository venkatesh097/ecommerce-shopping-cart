// ===================================================================
// Cart Page Logic (cart.html)
// ===================================================================

let activeCoupon = sessionStorage.getItem('ecom_coupon') || '';

document.addEventListener('DOMContentLoaded', () => {
    const input = document.getElementById('cart-coupon-input');
    if (input && activeCoupon) {
        input.value = activeCoupon;
    }
    loadCart();
});

async function loadCart() {
    const emptyView = document.getElementById('cart-empty-view');
    const contentView = document.getElementById('cart-content-view');
    const tbody = document.getElementById('cart-items-tbody');

    try {
        const cartKey = getCartKey();
        let url = `/cart?cartKey=${encodeURIComponent(cartKey)}`;
        if (activeCoupon) {
            url += `&couponCode=${encodeURIComponent(activeCoupon)}`;
        }

        const cart = await apiGet(url);
        await updateCartBadge();

        if (!cart.items || cart.items.length === 0) {
            emptyView.style.display = 'block';
            contentView.style.display = 'none';
            return;
        }

        emptyView.style.display = 'none';
        contentView.style.display = 'grid';

        // Render Item Rows
        tbody.innerHTML = '';
        cart.items.forEach(item => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>
                    <div class="item-title">${escapeText(item.productName)}</div>
                </td>
                <td>${formatCurrency(item.unitPrice)}</td>
                <td>
                    <div class="qty-control">
                        <button class="qty-btn" onclick="handleChangeQuantity(${item.productId}, ${item.quantity - 1})">-</button>
                        <span class="qty-display">${item.quantity}</span>
                        <button class="qty-btn" onclick="handleChangeQuantity(${item.productId}, ${item.quantity + 1})">+</button>
                    </div>
                </td>
                <td><strong>${formatCurrency(item.totalPrice)}</strong></td>
                <td>
                    <button class="btn btn-secondary btn-sm" onclick="handleRemoveItem(${item.productId})" title="Remove item">
                        Remove
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });

        // Render Totals Directly from Backend API
        document.getElementById('sum-subtotal').textContent = formatCurrency(cart.subtotal);

        const discountRow = document.getElementById('row-discount');
        const discountVal = parseFloat(cart.discountAmount || 0);
        if (discountVal > 0) {
            discountRow.style.display = 'flex';
            document.getElementById('label-discount').textContent = `Discount (${cart.appliedCoupon || 'Applied'})`;
            document.getElementById('sum-discount').textContent = `-${formatCurrency(cart.discountAmount)}`;
        } else {
            discountRow.style.display = 'none';
        }

        document.getElementById('sum-tax').textContent = formatCurrency(cart.taxAmount);
        document.getElementById('sum-total').textContent = formatCurrency(cart.grandTotal);

    } catch (err) {
        showMessage('Failed to load cart: ' + err.message, 'error');
    }
}

async function handleChangeQuantity(productId, newQty) {
    if (newQty <= 0) {
        return handleRemoveItem(productId);
    }
    try {
        const cartKey = getCartKey();
        await apiPatch(`/cart/items/${productId}?cartKey=${encodeURIComponent(cartKey)}`, {
            quantity: newQty
        });
        await loadCart();
    } catch (err) {
        showMessage(err.message, 'error');
    }
}

async function handleRemoveItem(productId) {
    try {
        const cartKey = getCartKey();
        await apiDelete(`/cart/items/${productId}?cartKey=${encodeURIComponent(cartKey)}`);
        showMessage('Item removed from cart', 'info');
        await loadCart();
    } catch (err) {
        showMessage(err.message, 'error');
    }
}

async function handleClearCart() {
    if (!confirm('Are you sure you want to clear your cart?')) return;
    try {
        const cartKey = getCartKey();
        await apiDelete(`/cart?cartKey=${encodeURIComponent(cartKey)}`);
        showMessage('Cart cleared', 'info');
        await loadCart();
    } catch (err) {
        showMessage(err.message, 'error');
    }
}

function handleApplyCoupon() {
    const input = document.getElementById('cart-coupon-input');
    const code = (input.value || '').trim().toUpperCase();
    activeCoupon = code;
    if (code) {
        sessionStorage.setItem('ecom_coupon', code);
        showMessage(`Applied coupon "${code}"`, 'info');
    } else {
        sessionStorage.removeItem('ecom_coupon');
        showMessage('Coupon removed', 'info');
    }
    loadCart();
}

function quickApplyCoupon(code) {
    const input = document.getElementById('cart-coupon-input');
    input.value = code;
    handleApplyCoupon();
}

function escapeText(str) {
    if (!str) return '';
    const p = document.createElement('p');
    p.textContent = str;
    return p.innerHTML;
}
