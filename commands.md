$env:POSTGRES_TEST_PASSWORD = Read-Host "Enter PostgreSQL password"

.\mvnw.cmd spring-boot:run

http://localhost:8080/ -> GET 

http://localhost:8080/api/products -> POST
{
    "id": 36,
    "name": "SONY - Camera",
    "description": "Camera with higher megapixel",
    "price": 189.9,
    "stockQuantity": 30,
    "category": "Photography",
    "imageUrl": ""
}
Then run get in the same url to see whether the product is applied or not in the  website

http://localhost:8080/api/cart/items -> POST 
{
    "productId": 36,
    "quantity": 3
}

