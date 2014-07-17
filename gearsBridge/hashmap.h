// taken from click-1.2.3
// ftp://ftp.mtu.ru/pub/FreeBSD-Archive/i386/4.9-RELEASE/packages/net/click-1.2.3.tgz
// we cannot use STL as this is/was not supported on Android

#ifndef HASHMAP_H
#define HASHMAP_H

#include <jni.h>
#include <string.h>

// K AND V REQUIREMENTS:
//
//              K::K()
//              K::operator bool() const
//                      Must have (bool)(K()) == false
//                      and no k with (bool)k == false is stored.
// K &          K::operator=(const K &)
//              k1 == k2
// int          hashcode(const K &)
//                      If hashcode(k1) != hashcode(k2), then k1 != k2.
//
//              V::V()
// V &          V::operator=(const V &)

template <class K, class V> class HashMapIterator;
template <class K, class V> struct Elt { K k; V v; Elt(): k(), v() { } };

template <class K, class V>
class HashMap { public:

  typedef HashMapIterator<K, V> Iterator;

  HashMap() : _capacity(0), _grow_limit(0), _n(0), _e(0), _default_v()
  {
    increase();
  }

  explicit HashMap(const V &def) : _capacity(0), _grow_limit(0), _n(0), _e(0), _default_v(def)
  {
    increase();
  }

  HashMap(const HashMap<K, V> &m) : _capacity(m._capacity), _grow_limit(m._grow_limit), _n(m._n),
		    _e(new Elt<K, V>*[m._capacity]), _default_v(m._default_v)
	{
	  for (int i = 0; i < _capacity; i++)
		if (0 != m._e[i]) {
			_e[i] = new Elt<K, V>();
			_e[i]->k = m._e[i]->k;
			_e[i]->v = m._e[i]->v;
		} else {
			_e[i] = 0;
		}
	}

  ~HashMap()                            { done(); }


  int size() const                      { return _n; }
  bool empty() const                    { return _n == 0; }
  int capacity() const                  { return _capacity; }

  const V &find(const K &) const;
  V *findp(const K &) const;
  const V &operator[](const K &k) const;

  bool insert(const K &, const V &);
  void remove(const K &key);
  void clear();

  Iterator first() const                { return Iterator(this); }

  HashMap<K, V> &operator=(const HashMap<K, V> &);

  void resize(int);
  void done();

 private:


  int _capacity;
  int _grow_limit;
  int _n;
  Elt<K, V> **_e;
  V _default_v;

  void increase();
  void check_capacity();
  int bucket(const K &) const;

  friend class HashMapIterator<K, V>;

};

template <class K, class V>
class HashMapIterator { public:

  HashMapIterator(const HashMap<K, V> *hm)  : _hm(hm)
  {
    Elt<K, V> **e = _hm->_e;
    int capacity = _hm->_capacity;
    for (_pos = 0; _pos < capacity && 0 == e[_pos]; _pos++)
      ;
  }


  operator bool() const                 { return _pos < _hm->_capacity; }
  void next();

  const K &key() const                  { return _hm->_e[_pos]->k; }
  V &value()                            { return _hm->_e[_pos]->v; }
  const V &value() const                { return _hm->_e[_pos]->v; }

 private:

  const HashMap<K, V> *_hm;
  int _pos;

};

template <class K, class V>
inline void
HashMap<K, V>::done() {
   for (int i = 0; i < _n; i++) {
	   if (0 != _e[i]) {
		   delete _e[i];
		   _e[i] = 0;
	   }
   }
   delete _e;
}

template <class K, class V>
inline const V &
HashMap<K, V>::find(const K &key) const
{
  int i = bucket(key);
  const V *v = (0 != _e[i] ? &_e[i]->v : &_default_v);
  return *v;
}

template <class K, class V>
inline const V &
HashMap<K, V>::operator[](const K &key) const
{
  return find(key);
}

template <class K, class V>
inline V *
HashMap<K, V>::findp(const K &key) const
{
  int i = bucket(key);
  return 0 != _e[i] ? &_e[i]->v : 0;
}

// ---------------------- hashcodes -------------------

inline int
hashcode(jlong l)
{
  return l < 0 ? -l : l;
}

#ifndef ENVIRONMENT64
inline long int
hashcode(long int i)
{
  return i < 0 ? -i : i;
}
#endif

inline int
hashcode(int i)
{
  return i < 0 ? -i : i;
}

inline unsigned
hashcode(unsigned u)
{
  return u;
}

inline int
hashcode(char* s)
{
  int h = 0;
  size_t len = strlen(s);
  for (size_t i = 0; i < len; i++) {
      h = 31*h + s[i];
  }
  return h;
}

// ---------------------------- equals ----------------------------

inline bool
equals(jlong i1, jlong i2)
{
  return i1 == i2;
}

#ifndef ENVIRONMENT64
inline bool
equals(long int i1, long int i2)
{
  return i1 == i2;
}
#endif

inline bool
equals(int i1, int i2)
{
  return i1 == i2;
}

inline bool
equals(unsigned u1, unsigned u2)
{
  return u1 == u2;
}

inline bool
equals(char* s1, char* s2)
{
  return s1 == s2 || 0 == strcmp(s1, s2);
}

// ----------------------------------------------------------------

template <class K, class V>
inline void
HashMap<K, V>::clear()
{
  done();
  _e = 0;
  _capacity = _grow_limit = _n = 0;
  increase();
}

template <class K, class V>
inline void
HashMap<K, V>::check_capacity()
{
  if (_n >= _grow_limit) increase();
}

template <class K, class V>
bool
HashMap<K, V>::insert(const K &key, const V &val)
{
  check_capacity();
  int i = bucket(key);
  bool is_new = 0 == _e[i];
  if (is_new) {
	  _e[i] = new Elt<K, V>();
  }
  _e[i]->k = key;
  _e[i]->v = val;
  _n += is_new;
  return is_new;
}

template <class K, class V>
void
HashMap<K, V>::remove(const K &key)
{
  int i = bucket(key);
  if (0 != _e[i]) {
	  delete _e[i];
	  _e[i] = 0;
  }
}

template <class K, class V>
inline int
HashMap<K, V>::bucket(const K &key) const
{
  int hc = hashcode(key);
  int i =   hc       & (_capacity - 1);
  int j = ((hc >> 6) & (_capacity - 1)) | 1;
  while (0 != _e[i] && !(equals(_e[i]->k, key))) {
    i = (i + j) & (_capacity - 1);
  }
  return i;
}

template <class K, class V>
inline HashMap<K, V> &
HashMap<K, V>::operator=(const HashMap<K, V> &o)
{
  // This works with self-assignment.

  _capacity = o._capacity;
  _grow_limit = o._grow_limit;
  _n = o._n;
  _default_v = o._default_v;

  Elt<K, V> **new_e = new Elt<K, V>*[_capacity];
  for (int i = 0; i < _capacity; i++) {
    new_e[i] = o._e[i];
  }

  done();
  _e = new_e;

  return *this;
}

template <class K, class V>
inline void
HashMap<K, V>::resize(int i)
{
  while (i > _capacity) increase();
}


template <class K, class V>
inline void
HashMap<K, V>::increase()
{
  Elt<K, V> **oe = _e;
  int ocap = _capacity;

  _capacity *= 2;
  if (_capacity < 8) _capacity = 8;
  _grow_limit = (int)(0.8 * _capacity) - 1;
  _e = new Elt<K, V>*[_capacity];
  for (int i = 0; i < _capacity; i++) {
  	_e[i] = 0;
  }
  for (int i = 0; i < ocap; i++) {
    if (0 != oe[i]) {
       int j = bucket(oe[i]->k);
       _e[j] = oe[i];
    }
  }
  if (0 != oe) {
      delete oe; // but not oe contents as rehashed
  }
}

template <class K, class V>
inline void
HashMapIterator<K, V>::next()
{
  Elt<K, V> **e = _hm->_e;
  int capacity = _hm->_capacity;
  for (_pos++; _pos < capacity && 0 == e[_pos]; _pos++)
    ;
}

#endif
