// ===================================================================
// Checkout Page Logic (checkout.html)
// ===================================================================

let checkoutCart = null;
const appliedCoupon = sessionStorage.getItem('ecom_coupon') || '';

document.addEventListener('DOMContentLoaded', () => {
    loadCheckoutSummary();
});

async function loadCheckoutSummary() {
    try {
        const cartKey = getCartKey();
        let url = `/cart?cartKey=${encodeURIComponent(cartKey)}`;
        if (appliedCoupon) {
            url += `&couponCode=${encodeURIComponent(appliedCoupon)}`;
        }

        const cart = await apiGet(url);
        checkoutCart = cart;

        if (!cart.items || cart.items.length === 0) {
            showMessage('Your cart is empty! Redirecting to products...', 'error');
            setTimeout(() => {
                window.location.href = 'index.html';
            }, 1500);
            return;
        }

        // Render preview item list
        const itemsList = document.getElementById('checkout-items-list');
        itemsList.innerHTML = '';
        cart.items.forEach(item => {
            const row = document.createElement('div');
            row.className = 'summary-row';
            row.innerHTML = `
                <span>${escapeText(item.productName)} &times; ${item.quantity}</span>
                <span>${formatCurrency(item.totalPrice)}</span>
            `;
            itemsList.appendChild(row);
        });

        // Pricing Breakdown from API
        document.getElementById('chk-subtotal').textContent = formatCurrency(cart.subtotal);

        const discountRow = document.getElementById('chk-discount-row');
        const discountVal = parseFloat(cart.discountAmount || 0);
        if (discountVal > 0) {
            discountRow.style.display = 'flex';
            document.getElementById('chk-discount-label').textContent = `Discount (${cart.appliedCoupon || 'Applied'})`;
            document.getElementById('chk-discount').textContent = `-${formatCurrency(cart.discountAmount)}`;
        } else {
            discountRow.style.display = 'none';
        }

        document.getElementById('chk-tax').textContent = formatCurrency(cart.taxAmount);
        document.getElementById('chk-total').textContent = formatCurrency(cart.grandTotal);

    } catch (err) {
        showMessage('Failed to load cart summary: ' + err.message, 'error');
    }
}

async function handlePlaceOrder(event) {
    event.preventDefault();

    const submitBtn = document.getElementById('btn-submit-order');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Processing Order...';

    const cartKey = getCartKey();
    const payload = {
        cartKey: cartKey,
        customerName: document.getElementById('cust-name').value.trim(),
        customerEmail: document.getElementById('cust-email').value.trim(),
        shippingAddress: document.getElementById('cust-address').value.trim(),
        couponCode: appliedCoupon || null,
        paymentMethod: document.getElementById('cust-payment').value
    };

    try {
        const order = await apiPost('/checkout', payload);

        // Clear local coupon state
        sessionStorage.removeItem('ecom_coupon');

        // Redirect to order confirmation page with the newly created order ID
        window.location.href = `order.html?id=${encodeURIComponent(order.id)}`;

    } catch (err) {
        showMessage(err.message, 'error');
        submitBtn.disabled = false;
        submitBtn.textContent = 'Place Order \u2192';
    }
}

function escapeText(str) {
    if (!str) return '';
    const p = document.createElement('p');
    p.textContent = str;
    return p.innerHTML;
}
