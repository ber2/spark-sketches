import struct

import numpy as np
import numpy.typing as npt

from .constants import BYTES_IN_A_LONG

def deserialize_minhash(binary: bytes) -> npt.NDArray[np.uint64]:
    byte_array = bytearray(binary)
    result_array_length = 2 * len(byte_array) // BYTES_IN_A_LONG
    buffer_format = f">{result_array_length}l"
    hashvalues = struct.unpack_from(buffer_format, byte_array)[1::2]
    return np.array(hashvalues, dtype=np.uint64)
