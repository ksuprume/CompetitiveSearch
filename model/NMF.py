import numpy as np
from sklearn.decomposition import NMF
import os
path = os.path.dirname(os.path.abspath(__file__))

X = np.loadtxt(path+'/raw_matrix.txt',dtype=np.float64)
for i in range(1, 10):
    model = NMF(n_components=i, init='random', random_state=0)
    W = model.fit_transform(X)
    H = model.components_
    np.savetxt(path+'/W'+str(i)+'.txt',W)
    np.savetxt(path+'/H'+str(i)+'.txt',H)
