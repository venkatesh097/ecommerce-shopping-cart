// ===================================================================
// Products Page Logic (index.html)
// ===================================================================

document.addEventListener('DOMContentLoaded', () => {
    loadProducts();
});

async function loadProducts() {
    const subtitle = document.getElementById('catalog-subtitle');
    const grid = document.getElementById('products-grid');
    const emptyBox = document.getElementById('empty-products');

    try {
        const products = await apiGet('/products');
        grid.innerHTML = '';

        if (!products || products.length === 0) {
            subtitle.textContent = '0 products available';
            emptyBox.style.display = 'block';
            return;
        }

        emptyBox.style.display = 'none';
        subtitle.textContent = `${products.length} product${products.length === 1 ? '' : 's'} available in store`;

        products.forEach(p => {
            const card = document.createElement('div');
            card.className = 'product-card';

            const inStock = p.stockQuantity && p.stockQuantity > 0;
            const priceFormatted = formatCurrency(p.price);

            const imgHtml = p.imageUrl
                ? `<img src="${escapeAttr(p.imageUrl)}" alt="${escapeAttr(p.name)}" class="product-image" onerror="this.onerror=null; this.parentElement.innerHTML='<span class=\\'product-image-placeholder\\'>🛍️</span>';">`
                : `<span class="product-image-placeholder">🛍️</span>`;

            card.innerHTML = `
                <div class="product-image-container">
                    ${imgHtml}
                </div>
                <div class="product-details">
                    <span class="product-category">${escapeText(p.category || 'General')}</span>
                    <h3 class="product-name">${escapeText(p.name)}</h3>
                    <p class="product-desc">${escapeText(p.description || '')}</p>
                    <div class="product-meta">
                        <span class="product-price">${priceFormatted}</span>
                        <span class="product-stock ${inStock ? '' : 'out'}">
                            ${inStock ? `${p.stockQuantity} in stock` : 'Out of stock'}
                        </span>
                    </div>
                    <button class="btn btn-primary btn-block" 
                            id="add-btn-${p.id}"
                            onclick="handleAddToCart(${p.id}, '${escapeAttr(p.name)}')" 
                            ${inStock ? '' : 'disabled'}>
                        ${inStock ? 'Add to Cart' : 'Out of Stock'}
                    </button>
                </div>
            `;
            grid.appendChild(card);
        });
    } catch (err) {
        subtitle.textContent = 'Error loading products';
        showMessage('Failed to load products: ' + err.message, 'error');
    }
}

async function handleAddToCart(productId, productName) {
    const btn = document.getElementById(`add-btn-${productId}`);
    if (btn) btn.disabled = true;

    try {
        const cartKey = getCartKey();
        await apiPost(`/cart/items?cartKey=${encodeURIComponent(cartKey)}`, {
            productId: productId,
            quantity: 1
        });
        showMessage(`Added "${productName}" to cart!`, 'success');
        await updateCartBadge();
    } catch (err) {
        showMessage(err.message, 'error');
    } finally {
        if (btn) btn.disabled = false;
    }
}

function escapeText(str) {
    if (!str) return '';
    const p = document.createElement('p');
    p.textContent = str;
    return p.innerHTML;
}

function escapeAttr(str) {
    if (!str) return '';
    return str.replace(/"/g, '&quot;');
}
