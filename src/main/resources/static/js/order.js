// ===================================================================
// Order Confirmation Page Logic (order.html)
// ===================================================================

document.addEventListener('DOMContentLoaded', () => {
    loadOrderDetails();
});

async function loadOrderDetails() {
    const card = document.getElementById('order-card-container');
    const errBox = document.getElementById('order-error-state');
    const errMsg = document.getElementById('order-error-msg');

    const params = new URLSearchParams(window.location.search);
    const orderId = params.get('id');

    if (!orderId) {
        errMsg.textContent = 'No order ID was specified in the URL.';
        errBox.style.display = 'block';
        return;
    }

    try {
        const order = await apiGet(`/orders/${encodeURIComponent(orderId)}`);

        // Populate order metadata
        document.getElementById('ord-number').textContent = order.orderNumber || `#${order.id}`;
        document.getElementById('ord-status').textContent = order.status || 'CONFIRMED';
        document.getElementById('ord-customer').textContent = order.customerName || '-';
        document.getElementById('ord-email').textContent = order.customerEmail || '-';
        document.getElementById('ord-address').textContent = order.shippingAddress || '-';

        // Populate items
        const tbody = document.getElementById('ord-items-tbody');
        tbody.innerHTML = '';
        if (order.items && order.items.length > 0) {
            order.items.forEach(item => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><strong>${escapeText(item.productName)}</strong></td>
                    <td>${formatCurrency(item.unitPrice)}</td>
                    <td>${item.quantity}</td>
                    <td>${formatCurrency(item.totalPrice)}</td>
                `;
                tbody.appendChild(tr);
            });
        }

        // Populate totals directly from API response
        document.getElementById('ord-subtotal').textContent = formatCurrency(order.subtotal);

        const discountRow = document.getElementById('ord-discount-row');
        const discountVal = parseFloat(order.discountAmount || 0);
        if (discountVal > 0) {
            discountRow.style.display = 'flex';
            document.getElementById('ord-discount').textContent = `-${formatCurrency(order.discountAmount)}`;
        } else {
            discountRow.style.display = 'none';
        }

        document.getElementById('ord-tax').textContent = formatCurrency(order.taxAmount);
        document.getElementById('ord-total').textContent = formatCurrency(order.totalAmount);

        // Display card
        card.style.display = 'block';
        errBox.style.display = 'none';

        // Update cart badge (should be 0 post-checkout)
        await updateCartBadge();

    } catch (err) {
        errMsg.textContent = err.message;
        errBox.style.display = 'block';
        card.style.display = 'none';
    }
}

function escapeText(str) {
    if (!str) return '';
    const p = document.createElement('p');
    p.textContent = str;
    return p.innerHTML;
}
