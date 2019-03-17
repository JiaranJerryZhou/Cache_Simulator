#ifndef __CACHE_
#define __CACHE_
#include <cstdlib>
#include <vector>
class block
{
 public:
  bool valid;
  bool dirty;
  long tag;
  int lastAccessTime;

  block() {
    valid = false;
    dirty = false;
    lastAccessTime = -2147483648;
  }
  block & operator=(block & b)

  {
    valid = b.valid;
    dirty = b.dirty;
    tag = b.tag;
    lastAccessTime = b.lastAccessTime;
    return *this;
  }
};
class cache
{
 public:
  int associativity;
  int block_size;
  int capacity;
  int sets_number;
  std::vector<std::vector<block> > caches;

  int fetch_number, read_number, write_number;
  int fetch_hit, read_hit, write_hit;
  int fetch_miss, read_miss, write_miss;
  int currentTime;
  int strategy;
  int writeScheme;  // 0 means write-allocate, 1 means write-no-allocate
  int level;
  cache(){};
  cache(int A, int B, int C, int strat, int WScheme, int lv) {
    associativity = A;
    block_size = B;
    capacity = C;
    fetch_number = read_number = write_number = 0;
    fetch_hit = read_hit = write_hit = 0;
    fetch_miss = read_miss = write_miss = 0;
    currentTime = 0;
    strategy = strat;
    writeScheme = WScheme;
    level = lv;

    sets_number = capacity / (block_size * associativity);

    caches.resize(sets_number);
    for (int k = 0; k < sets_number; ++k) {
      caches[k].resize(associativity);
    }

    for (int i = 0; i < sets_number; i++) {
      for (int j = 0; j < associativity; j++) {
        block * newblock = new block();
        caches[i].push_back(*newblock);
      }
    }
  }

  ~cache() {}
};

#endif
