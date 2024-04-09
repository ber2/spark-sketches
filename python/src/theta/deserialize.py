import struct

from .theta import Theta


def deserialize(xs: bytes) -> Theta:
    byte_array = bytearray(xs)
    theta_bytes = byte_array[0:8]
    hashes_length_bytes = byte_array[8:12]
    hash_bytes = byte_array[12:]

    theta = struct.unpack_from(">d", theta_bytes)[0]
    hashes_length = struct.unpack_from(">i", hashes_length_bytes)[0]
    hash_values = set(struct.unpack_from(f">{hashes_length}q", hash_bytes))
    return Theta(theta, hash_values)
