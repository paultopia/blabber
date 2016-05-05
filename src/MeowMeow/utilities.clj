;; helper functions that show up in multiple namespaces.

(ns MeowMeow.utilities)

(defn- numdocs-t-helper
  "given a column represented as a bunch of individual entries in that column, 
  (NOT as a vector) returns the number of entries that are above zero"
  [& args]
  (count (filter #(< 0 %) args)))

(defn ccp
  "this is the count of documents in which a given token appears"
  [tdm]
  (apply map numdocs-t-helper tdm))

  ;; note to self for future reference: this is the same sneaky trick that lets you 
  ;; transpose a matrix by (apply map vector matrix) --- map when given n sequences 
  ;; takes the first from each sequence, then the second from each sequence, etc. -- 
  ;; so when given map applied over a sequence of rows, it's like mapping over 
  ;; a sequence of columns without apply
  
  (def count-column-presences (memoize ccp))
  
  (defn filter-by-other 
    [target test pred]
    (let [pairs (map vector target test)]
      (mapv first (filter #(pred (second %)) pairs))))

;; filters target based on applying pred to test.  probably will blow up 
;; with infinite sequences, and god only knows what will 
;; happen with lazy ones.  Assumes target and test are vectors of equal length, 
;; pred is a single-arity function 

