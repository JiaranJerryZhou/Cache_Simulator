#ifndef __CACHE_SIMU_
#define __CACHE_SIMU_
#include <stdlib.h>
#include <time.h>

#include <cstdio>
#include <cstdlib>
#include <iomanip>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>

#include "cache.h"
using namespace std;
class cache_simu
{
 public:
  int random;
  int LRU;
  int write_allocate;
  int write_no_allocate;
  vector<cache> L1cache;
  vector<cache> L2cache;
  bool L1_unified, L2_unified;
  int levels_number;
  float L1_hitTime, L2_hitTime, DRAM_accessTime;
  int records_number;
  cache_simu() {
    random = 0;
    LRU = 1;
    write_allocate = 0;
    write_no_allocate = 1;
  }
  cache_simu(int nlv,
             bool L1_u,
             bool L2_u,
             string L1,
             string L2,
             float L1_h,
             float L2_h,
             float DRAM_a) {
  	random = 0;
    LRU = 1;
    write_allocate = 0;
    write_no_allocate = 1;
    levels_number = nlv;
    L1_unified = L1_u;
    L1_hitTime = L1_h;
    L2_unified = L2_u;
    L2_hitTime = L2_h;
    DRAM_accessTime = DRAM_a;
    records_number = 0;
    L1cache.clear();
    L2cache.clear();
    if (L1_unified) {
      vector<string> L1_configuration;
      istringstream iss(L1);
      for (string L1; iss >> L1;) {
        L1_configuration.push_back(L1);
      }
      int L1_A = stoi(L1_configuration[0]);
      int L1_B = stoi(L1_configuration[1]);
      int L1_C = stoi(L1_configuration[2]);
      int L1_strategy = stoi(L1_configuration[3]);
      int L1_writeScheme = stoi(L1_configuration[4]);
      cache newcache(L1_A, L1_B, L1_C, L1_strategy, L1_writeScheme, 0);

      L1cache.push_back(newcache);
      //      cout << L1cache[0].associativity << L1cache[0].block_size << L1cache[0].capacity << endl;
    }
    else {
      //L1cache[0] is IMEM, L1cache[1] is DMEM, the same mechanism for L2
      vector<string> L1_configuration;
      istringstream iss(L1);
      for (string L1; iss >> L1;) {
        L1_configuration.push_back(L1);
        // cout << L1 << endl;
      }
      int L1_I_A = stoi(L1_configuration[0]);
      int L1_I_B = stoi(L1_configuration[1]);
      int L1_I_C = stoi(L1_configuration[2]);
      int L1_I_strategy = stoi(L1_configuration[3]);
      int L1_I_writeScheme = stoi(L1_configuration[4]);
      int L1_D_A = stoi(L1_configuration[5]);
      int L1_D_B = stoi(L1_configuration[6]);
      int L1_D_C = stoi(L1_configuration[7]);
      int L1_D_strategy = stoi(L1_configuration[8]);
      int L1_D_writeScheme = stoi(L1_configuration[9]);
      cache Icache(L1_I_A, L1_I_B, L1_I_C, L1_I_strategy, L1_I_writeScheme, 0);
      L1cache.push_back(Icache);
      cache Dcache(L1_D_A, L1_D_B, L1_D_C, L1_D_strategy, L1_D_writeScheme, 0);
      L1cache.push_back(Dcache);
    }

    if (levels_number > 1) {
      if (L2_unified) {
        vector<string> L2_configuration;
        istringstream iss(L2);
        for (string L2; iss >> L2;) {
          L2_configuration.push_back(L2);
          //cout << L2 << endl;
        }
        int L2_A = stoi(L2_configuration[0]);
        int L2_B = stoi(L2_configuration[1]);
        int L2_C = stoi(L2_configuration[2]);
        int L2_strategy = stoi(L2_configuration[3]);
        int L2_writeScheme = stoi(L2_configuration[4]);
        cache newcache(L2_A, L2_B, L2_C, L2_strategy, L2_writeScheme, 1);
        L2cache.push_back(newcache);
        // cout << L2cache[0].associativity << L2cache[0].block_size << L2cache[0].capacity << endl;
      }
      else {
        vector<string> L2_configuration;
        istringstream iss(L2);
        for (string L2; iss >> L2;) {
          L2_configuration.push_back(L2);
          //cout << L2 << endl;
        }
        int L2_I_A = stoi(L2_configuration[0]);
        int L2_I_B = stoi(L2_configuration[1]);
        int L2_I_C = stoi(L2_configuration[2]);
        int L2_I_strategy = stoi(L2_configuration[3]);
        int L2_I_writeScheme = stoi(L2_configuration[4]);
        int L2_D_A = stoi(L2_configuration[5]);
        int L2_D_B = stoi(L2_configuration[6]);
        int L2_D_C = stoi(L2_configuration[7]);
        int L2_D_strategy = stoi(L2_configuration[8]);
        int L2_D_writeScheme = stoi(L2_configuration[9]);
        cache Icache(L2_I_A, L2_I_B, L2_I_C, L2_I_strategy, L2_I_writeScheme, 1);
        L2cache.push_back(Icache);
        cache Dcache(L2_D_A, L2_D_B, L2_D_C, L2_D_strategy, L2_D_writeScheme, 1);
        L2cache.push_back(Dcache);
      }
      //      L2_unified = L2_u;
      // L2_hitTime = L2_h;
    }
  }

  void parse(string line) {
    vector<string> s;
    istringstream iss(line);
    for (string line; iss >> line;) {
      s.push_back(line);
    }
    int code = stoi(s[0]);

    float address = stol(s[1], nullptr, 16);

    records_number++;

    if (code == 0) {
      // cout<<"r"<<endl;
      if (L1_unified) {
        Read(address, 0, L1cache[0]);
      }
      else {
        Read(address, 0, L1cache[1]);
      }
    }
    else if (code == 1) {
      // cout<<"w"<<endl;
      if (L1_unified) {
        Write(address, 0, L1cache[0]);
      }
      else {
        Write(address, 0, L1cache[1]);
      }
    }
    else {
      //cout<<"f"<<endl;
      Fetch(address, 0, L1cache[0]);
    }
  }
  int random_index(int size) {
    srand(time(NULL));

    int RandIndex = rand() % size;

    return RandIndex;
  }

  int LRU_index(vector<block> &C) {
    int LRU = 2147483647;
    int index = 0;

    for (size_t i = 0; i < C.size(); i++) {
      block data = C[i];
      if (data.lastAccessTime < LRU) {
        LRU = data.lastAccessTime;
        index = i;
      }
    }

    return index;
  }
  void Read(float address, int level, cache & C) {
    C.read_number++;
    long a = address / C.block_size;
    long tag = a / C.sets_number;
    long index = a % C.sets_number;
    //    cout << tag << endl;
    for (int col = 0; col < C.associativity; col++) {
      block * data = &C.caches[(int)index][col];
      //      cout << data->tag << " " << endl;
      if (data->valid && data->tag == tag) {
        C.read_hit++;
        //cout << "read_hit" << endl;
        data->lastAccessTime = C.currentTime;

        break;
      }
      else if (C.associativity == 1) {
        C.read_miss++;
        //cout << "read_miss1" << endl;
        if (data->valid && data->dirty && level == 0 && levels_number > 1) {
          long address_toWrite = (data->tag * C.sets_number + index) * C.block_size;
          if (L2_unified) {
            Write(address_toWrite, 1, L2cache[0]);
          }
          else {
            Write(address_toWrite, 1, L2cache[1]);
          }
        }
        if (level == 0 && levels_number > 1) {
          if (L2_unified) {
            Read(address, 1, L2cache[0]);
          }
          else {
            Read(address, 1, L2cache[1]);
          }
        }
        data->tag = tag;
        // cout << data.tag << endl;
        data->valid = true;
        data->dirty = false;
        data->lastAccessTime = C.currentTime;

        break;
      }
      else if (col == C.associativity - 1) {
        C.read_miss++;
        //cout << "read_miss2" << endl;
        int col_evicted;
        block * data_evicted;
        if (C.strategy == random) {
          col_evicted = random_index(C.associativity);
          data_evicted = &C.caches[(int)index][col_evicted];
          if (data_evicted->valid && data_evicted->dirty && level == 0 && levels_number > 1) {
            float address_toWrite = (data_evicted->tag * C.sets_number + index) * C.block_size;
            if (L2_unified) {
              Write(address_toWrite, 1, L2cache[0]);
            }
            else {
              Write(address_toWrite, 1, L2cache[1]);
            }
          }
          // LRU
        }
        else {
          col_evicted = LRU_index(C.caches[(int)index]);
          data_evicted = &C.caches[(int)index][col_evicted];
          if (data_evicted->valid && data_evicted->dirty && level == 0 && levels_number > 1) {
            float address_toWrite = (data_evicted->tag * C.sets_number + index) * C.block_size;
            if (L2_unified) {
              Write(address_toWrite, 1, L2cache[0]);
            }
            else {
              Write(address_toWrite, 1, L2cache[1]);
            }
          }
        }
        if (level == 0 && levels_number > 1) {
          if (L2_unified) {
            Read(address, 1, L2cache[0]);
          }
          else {
            Read(address, 1, L2cache[1]);
          }
        }
        data_evicted->tag = tag;
        data_evicted->valid = true;
        data_evicted->dirty = false;
        data_evicted->lastAccessTime = C.currentTime;
      }
    }

    C.currentTime++;
  }

  void Write(float address, int level, cache & C) {
    C.write_number++;
    long a = address / C.block_size;
    long tag = a / C.sets_number;
    long index = a % C.sets_number;
    // cout << a << " " << tag << " " << index << endl;
    for (int col = 0; col < C.associativity; col++) {
      block * data = &C.caches[(int)index][col];
      // write hit
      // cout << data.tag << endl;
      if (data->valid && data->tag == tag) {
        C.write_hit++;
        //cout << "write_hit" << endl;
        data->dirty = true;
        data->lastAccessTime = C.currentTime;
        break;
        // write miss and direct-mapped
      }
      else if (C.associativity == 1) {
        C.write_miss++;
        //cout << "write_miss1" << endl;
        if (C.writeScheme == write_allocate) {
          if (data->valid && data->dirty && level == 0 && levels_number > 1) {
            long address_toWrite = (data->tag * C.sets_number + index) * C.block_size;
            if (L2_unified) {
              Write(address_toWrite, 1, L2cache[0]);
            }
            else {
              Write(address_toWrite, 1, L2cache[1]);
            }
          }
          if (level == 0 && levels_number > 1) {
            if (L2_unified) {
              Read(address, 1, L2cache[0]);
            }
            else {
              Read(address, 1, L2cache[1]);
            }
          }
          data->tag = tag;
          data->valid = true;
          data->dirty = true;
          data->lastAccessTime = C.currentTime;
        }
        else {
          if (levels_number > 1) {
            if (L2_unified) {
              Write(address, 1, L2cache[0]);
            }
            else {
              Write(address, 1, L2cache[1]);
            }
          }
        }
        break;
        // write miss and not direct-mapped
      }
      else if (col == C.associativity - 1) {
        C.write_miss++;
        //cout << "write_miss2" << endl;
        int col_evicted;
        block * data_evicted;
        if (C.writeScheme == 0) {
          if (C.strategy == random) {
            col_evicted = random_index(C.associativity);
            data_evicted = &C.caches[(int)index][col_evicted];
            if (data_evicted->valid && data_evicted->dirty && level == 0 && levels_number > 1) {
              long address_toWrite = (data_evicted->tag * C.sets_number + index) * C.block_size;
              if (L2_unified) {
                Write(address_toWrite, 1, L2cache[0]);
              }
              else {
                Write(address_toWrite, 1, L2cache[1]);
              }
            }
            if (level == 0 && levels_number > 1) {
              if (L2_unified) {
                Read(address, 1, L2cache[0]);
              }
              else {
                Read(address, 1, L2cache[1]);
              }
            }
          }
          else {
            col_evicted = LRU_index(C.caches[(int)index]);
            data_evicted = &C.caches[(int)index][col_evicted];
            if (data_evicted->valid && data_evicted->dirty && level == 0 && levels_number > 1) {
              long address_toWrite = (data_evicted->tag * C.sets_number + index) * C.block_size;
              if (L2_unified) {
                Write(address_toWrite, 1, L2cache[0]);
              }
              else {
                Write(address_toWrite, 1, L2cache[1]);
              }
            }
            if (level == 0 && levels_number > 1) {
              if (L2_unified) {
                Read(address, 1, L2cache[0]);
              }
              else {
                Read(address, 1, L2cache[1]);
              }
            }
          }
          data_evicted->tag = tag;
          data_evicted->valid = true;
          data_evicted->dirty = true;
          data_evicted->lastAccessTime = C.currentTime;
        }
        else {
          if (level == 0 && levels_number > 1) {
            if (L2_unified) {
              Write(address, 1, L2cache[0]);
            }
            else {
              Write(address, 1, L2cache[1]);
            }
          }
        }
      }
    }

    C.currentTime++;
  }
  void Fetch(float address, int level, cache & C) {
    C.fetch_number++;
    long a = address / C.block_size;
    long tag = a / C.sets_number;
    long index = a % C.sets_number;
    //cout << a << " " << tag << " " << index << endl;
    for (int col = 0; col < C.associativity; col++) {
      block * data = &C.caches[(int)index][col];
      // cout << data.tag << endl;
      if (data->valid && data->tag == tag) {
        C.fetch_hit++;
        //cout << "fetch_hit" << endl;
        data->lastAccessTime = C.currentTime;
        break;
      }
      else if (C.associativity == 1) {
        C.fetch_miss++;
        //cout << "fetch_miss1" << endl;
        if (level == 0 && levels_number > 1) {
          Fetch(address, 1, L2cache[0]);
        }
        data->tag = tag;
        data->valid = true;
        data->dirty = false;
        data->lastAccessTime = C.currentTime;
        break;
      }
      else if (col == C.associativity - 1) {
        C.fetch_miss++;
        //cout << "fetch_miss2" << endl;
        int col_evicted;
        block * data_evicted;
        // Pick random one to evict
        if (C.strategy == random) {
          col_evicted = random_index(C.associativity);
          data_evicted = &C.caches[(int)index][col_evicted];
          // LRU
        }
        else {
          col_evicted = LRU_index(C.caches[(int)index]);
          data_evicted = &C.caches[(int)index][col_evicted];
        }
        if (level == 0 && levels_number > 1) {
          Fetch(address, 1, L2cache[0]);
        }
        data_evicted->tag = tag;
        data_evicted->valid = true;
        data_evicted->dirty = false;
        data_evicted->lastAccessTime = C.currentTime;
      }
    }

    C.currentTime++;
  }
  void print() {
    cout << "L1:" << endl;
    cout << "----------------------------------" << endl;
    cout << setiosflags(ios::left) << setw(20) << "Matrics" << resetiosflags(ios::left)
         << setiosflags(ios::right) << setw(8) << "Total" << setw(8) << "Insn" << setw(8) << "Data"
         << setw(8) << "Read" << setw(8) << "Write" << resetiosflags(ios::right) << endl;
    cout << "----------------------------------" << endl;
    int total, data, fetch, read, write;
    double fetch_rate, read_rate, write_rate, data_rate;
    int total_miss, data_miss, fetch_miss, read_miss, write_miss;
    double total_miss_rate, data_miss_rate, fetch_miss_rate, read_miss_rate, write_miss_rate;
    if (L1_unified) {
      read = L1cache[0].read_number;
      fetch = L1cache[0].fetch_number;
      write = L1cache[0].write_number;
      read_miss = L1cache[0].read_miss;
      write_miss = L1cache[0].write_miss;
      fetch_miss = L1cache[0].fetch_miss;
    }
    else {
      read = L1cache[0].read_number + L1cache[1].read_number;
      fetch = L1cache[0].fetch_number + L1cache[1].fetch_number;
      write = L1cache[0].write_number + L1cache[1].write_number;
      read_miss = L1cache[1].read_miss;
      write_miss = L1cache[1].write_miss;
      fetch_miss = L1cache[0].fetch_miss;
    }
    total = read + write + fetch;
    data = read + write;
    fetch_rate = (double)fetch / (double)total;
    read_rate = (double)read / (double)total;
    write_rate = (double)write / (double)total;
    data_rate = (double)data / (double)total;
    total_miss = fetch_miss + read_miss + write_miss;
    data_miss = read_miss + write_miss;
    total_miss_rate = (double)total_miss / (double)total;
    data_miss_rate = (double)data_miss / (double)data;
    read_miss_rate = (double)read_miss / (double)read;
    write_miss_rate = (double)write_miss / (double)write;
    fetch_miss_rate = (double)fetch_miss / (double)fetch;
    //first line
    cout << setiosflags(ios::left) << setw(20) << "Demand Fetches" << resetiosflags(ios::left)
         << setiosflags(ios::right) << setw(8) << total << setw(8) << fetch << setw(8) << data
         << setw(8) << read << setw(8) << write << resetiosflags(ios::right) << endl;
    //second line
    cout << setiosflags(ios::left) << setw(20) << "Fraction of total" << resetiosflags(ios::left)
         << setiosflags(ios::right) << setw(8) << setiosflags(ios::fixed) << "1.0000" << setw(8)
         << setprecision(4) << fetch_rate << setw(8) << data_rate << setw(8) << read_rate << setw(8)
         << write_rate << resetiosflags(ios::right) << resetiosflags(ios::fixed) << endl;
    cout << endl;
    //third line
    cout << setiosflags(ios::left) << setw(20) << "Demand Misses" << resetiosflags(ios::left)
         << setiosflags(ios::right) << setw(8) << total_miss << setw(8) << fetch_miss << setw(8)
         << data_miss << setw(8) << read_miss << setw(8) << write_miss << resetiosflags(ios::right)
         << endl;
    //fourth line
    cout << setiosflags(ios::left) << setw(20) << "Demand Miss Rate" << resetiosflags(ios::left)
         << setiosflags(ios::right) << setw(8) << setiosflags(ios::fixed) << setprecision(4)
         << total_miss_rate << setw(8) << fetch_miss_rate << setw(8) << data_miss_rate << setw(8)
         << read_miss_rate << setw(8) << write_miss_rate << resetiosflags(ios::right)
         << resetiosflags(ios::fixed) << endl;
    cout << endl;
    //print for L2
    if (levels_number > 1) {
      cout << "L2:" << endl;
      cout << "----------------------------------" << endl;
      cout << setiosflags(ios::left) << setw(20) << "Matrics" << resetiosflags(ios::left)
           << setiosflags(ios::right) << setw(8) << "Total" << setw(8) << "Insn" << setw(8)
           << "Data" << setw(8) << "Read" << setw(8) << "Write" << resetiosflags(ios::right)
           << endl;
      cout << "----------------------------------" << endl;
      int total, data, fetch, read, write;
      double fetch_rate, read_rate, write_rate, data_rate;
      int total_miss, data_miss, fetch_miss, read_miss, write_miss;
      double total_miss_rate, data_miss_rate, fetch_miss_rate, read_miss_rate, write_miss_rate;
      if (L2_unified) {
        read = L2cache[0].read_number;
        fetch = L2cache[0].fetch_number;
        write = L2cache[0].write_number;
        read_miss = L2cache[0].read_miss;
        write_miss = L2cache[0].write_miss;
        fetch_miss = L2cache[0].fetch_miss;
      }
      else {
        read = L2cache[0].read_number + L2cache[1].read_number;
        fetch = L2cache[0].fetch_number + L2cache[1].fetch_number;
        write = L2cache[0].write_number + L2cache[1].write_number;
        read_miss = L2cache[1].read_miss;
        write_miss = L2cache[1].write_miss;
        fetch_miss = L2cache[0].fetch_miss;
      }
      total = read + write + fetch;
      data = read + write;
      fetch_rate = (double)fetch / (double)total;
      read_rate = (double)read / (double)total;
      write_rate = (double)write / (double)total;
      data_rate = (double)data / (double)total;
      total_miss = fetch_miss + read_miss + write_miss;
      data_miss = read_miss + write_miss;
      total_miss_rate = (double)total_miss / (double)total;
      data_miss_rate = (double)data_miss / (double)data;
      read_miss_rate = (double)read_miss / (double)read;
      write_miss_rate = (double)write_miss / (double)write;
      fetch_miss_rate = (double)fetch_miss / (double)fetch;
      //first line
      cout << setiosflags(ios::left) << setw(20) << "Demand Fetches" << resetiosflags(ios::left)
           << setiosflags(ios::right) << setw(8) << total << setw(8) << fetch << setw(8) << data
           << setw(8) << read << setw(8) << write << resetiosflags(ios::right) << endl;
      //second line
      cout << setiosflags(ios::left) << setw(20) << "Fraction of total" << resetiosflags(ios::left)
           << setiosflags(ios::right) << setw(8) << setiosflags(ios::fixed) << "1.0000"
           << setprecision(4) << setw(8) << fetch_rate << setw(8) << data_rate << setw(8)
           << read_rate << setw(8) << write_rate << resetiosflags(ios::right)
           << resetiosflags(ios::fixed) << endl;
      cout << endl;
      //third line
      cout << setiosflags(ios::left) << setw(20) << "Demand Misses" << resetiosflags(ios::left)
           << setiosflags(ios::right) << setw(8) << total_miss << setw(8) << fetch_miss << setw(8)
           << data_miss << setw(8) << read_miss << setw(8) << write_miss
           << resetiosflags(ios::right) << endl;
      //fourth line
      cout << setiosflags(ios::left) << setw(20) << "Demand Miss Rate" << resetiosflags(ios::left)
           << setiosflags(ios::right) << setw(8) << setiosflags(ios::fixed) << setprecision(4)
           << total_miss_rate << setw(8) << fetch_miss_rate << setw(8) << data_miss_rate << setw(8)
           << read_miss_rate << setw(8) << write_miss_rate << resetiosflags(ios::right)
           << resetiosflags(ios::fixed) << endl;
      cout << endl;
    }
  }

  ~cache_simu() {}
};
#endif
