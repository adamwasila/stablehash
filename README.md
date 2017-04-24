# hashring

Consistent hashing implementation in java

Implements consistent hashing that can be used when the number of server nodes can increase or decrease (like in memcached). The hashing ring is built using the same algorithm as libketama.

This is almost direct translation of golang hashring library https://github.com/serialx/hashring . 

# Using

//TODO

# Motivation

The main goal for this project was to have such minimal no-deps library to calculate distributed hashes. For now it supports only what original library had - consistent hashing, but there are plans to add rendez-vous (aka HRW) hashing implementation in the future with some common interface.

# Licensing

Licensed under the Apache License, Version 2.0
