import bcrypt
password = b"Admin123!"
hashed = bcrypt.hashpw(password, bcrypt.gensalt(10))
print(hashed.decode())
