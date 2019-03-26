import numpy as np
import math
import random


def init_centers_sampling(points, k, seed=False):
    """ 
    points: a 2d numpy array where each row is a point.
    k: number of cluster centers for k-means.
    Returns a 2d numpy array where each row is a cluster center,
    found by randomly sampling from points.
    """
    if seed:
        random.seed(seed)
    return random.sample(list(points), k)


def assign_nearest(points, centers, seed=False):
    """
    points: a 2d numpy array where each row is a point.
    centers: a 2d numpy array where each row is a cluster center.
    Returns an array of indices of closest center for each point in points.
    """
    # for each point, find closest center
    return [
        np.argmin(
            [np.linalg.norm(point - center) for center in centers]
        ) for point in points
    ]


def update_centers(points, assignments, centers):
    """
    points: a 2d numpy array where each row is a point.
    assignments: array of indices of closest center for each point in points.
    centers: a 2d numpy array where each row is a cluster center.
    Returns none; updates centers.
    """
    points_by_center = [[] for _ in range(len(centers))]
    # Gather all points assigned to a given center
    for i, point in enumerate(points):
        points_by_center[assignments[i]].append(point)
    # Assign centers to mean of assigned points, if any assigned
    for i, gathered_points in enumerate(points_by_center):
        if len(gathered_points) > 0:
            centers[i] = np.average(gathered_points, axis=0)


def calc_objective(points, assignments, centers):
    return np.sum([
        np.linalg.norm(points[i] - centers[assignments[i]])
        for i in range(len(points))
    ])


def k_means(points, k, max_epochs=1000, seed=False):
    """ 
    points: a 2d numpy array where each row is a point.
    k: number of cluster centers for k-means.
    Returns a 2d numpy array where each row is a cluster center.
    If verbose flag is True:
    Returns an array of tuples of (centers, assignments).
    """
    # Initialization
    centers = init_centers_sampling(points, k, seed=seed)
    assignments = assign_nearest(points, centers)

    # Training: Iterate until convergence (when assignments don't change)
    last_assignments = assignments
    for step in range(1, max_epochs + 1):
        update_centers(points, assignments, centers)
        assignments = assign_nearest(points, centers)
        # Convergence?
        if last_assignments == assignments:
            break
        last_assignments = assignments

    if step < max_epochs:
        print('Converged after', step, 'iterations.')
    else:
        print('Never converged - stopped after', max_epochs, 'iterations.')

    objective = calc_objective(points, assignments, centers)
    # return assignments
    return objective


if __name__ == '__main__':
    import sys
    filename = sys.argv[1]

    with open(filename, 'r') as dataset:
        next(dataset) # Skip first line
        points = []
        for line in dataset:
            points.append([float(d) for d in line.split()[1:]]) # Skip ID

    # Find the elbow
    import matplotlib.pyplot as plt
    plt.plot([k_means(np.array(points), k) for k in range(1, 15)])
    plt.title(filename)
    plt.ylabel('Objective function')
    plt.xlabel('k-1 (add 1 to get k)')
    plt.show()