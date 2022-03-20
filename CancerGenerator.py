import os
import math
import random
import datetime
from pathlib import Path
import sys

#train_size = 80
#test_size = 100


def create_set(size, prefix, groupCount=None, groupSize=None, fixedParam=''):
    if groupCount == 'None':
        group_count = int(math.ceil(math.sqrt(int(size))))
    else:
        group_count = int(groupCount)
    if groupSize == 'None':
        group_size = int(math.ceil(int(size) / int(group_count)))
    else:
        group_size = int(groupSize)
        group_count = int(math.ceil(float(size) / int(group_size)))
    size=int(size)
    print("Anzahl Gruppen: {}".format(group_count))
    print("Gruppengröße: {}".format(group_size))

    groups = []
    graph_group = {}
    for i in range(group_count):
        rand_num = random.randint(0, 9)
        label = "smoke" if rand_num < 3 else "not_smoke"
        groups.append(label)
        graph_group[i] = []

    friends = []
    graph_friends = []
    for a in range(size):
        for b in range(a, size):
            if a == b:
                continue
            rand_num = random.randint(0, 9)
            rand_num2 = random.randint(0,9)
            same_group = get_group_for_person(a, group_size) == get_group_for_person(b, group_size)
            if same_group and rand_num < 8:
                friends.append("friends({},{})".format(get_person_name(a), get_person_name(b)))
                friends.append("friends({},{})".format(get_person_name(b), get_person_name(a)))

                graph_friends.append("{} -- {}".format(get_person_name(a).replace("Person_",""), get_person_name(b).replace("Person_", "")))

            if not same_group and rand_num2 < 0:
                friends.append("friends({},{})".format(get_person_name(a), get_person_name(b)))
                friends.append("friends({},{})".format(get_person_name(b), get_person_name(a)))
                graph_friends.append("{} -- {}".format(get_person_name(a).replace("Person_",""), get_person_name(b).replace("Person_", "")))


    smoking = []
    not_smoking = []
    cancer = []
    not_cancer = []
    s_smokes = []
    not_s_smkoes = []
    # not_s_smokes_vbl = []
    for i in range(size):
        rand_num1 = random.randint(0, 9)
        rand_num2 = random.randint(0, 9)
        group_number = get_group_for_person(i, group_size)
        if (groups[group_number] == "smoke" and rand_num1 < 7) or rand_num1 < 1:
            smoking.append("smokes({})".format(get_person_name(i)))
            graph_group[group_number].append("{}[ fillcolor=crimson]".format(i))
            s_smokes.append("s_smokes({})".format(get_person_name(i)))
            #if rand_num2 < 5:
            #    cancer.append("cancer({})".format(get_person_name(i)))
            #else:
            #    not_cancer.append("cancer({})".format(get_person_name(i)))
        else:
            not_smoking.append("not_smokes({}).".format(get_person_name(i)))
            graph_group[group_number].append("{}[ fillcolor=aquamarine3]".format(i))
            not_s_smkoes.append("not_s_smokes({}).".format(get_person_name(i)))
            if rand_num2 < 1:
                cancer.append("cancer({})".format(get_person_name(i)))
            else:
                not_cancer.append("cancer({})".format(get_person_name(i)))
        #if i % group_size - 1 == 0 or i % group_size - 2 == 0:
        #    person = get_person_name(i)
        #    if any(str(person) in s for s in smoking):
        #        s_smokes.append("s_smokes({})".format(person))
        #        smoking.remove("smokes({})".format(person))
        #        group = get_group_for_person(int(person.replace("Person_", "")), group_size)
        #        for i in graph_group[group]:
        #            if any(str(person) in j for j in i):
        #                graph_group[group].pop("{}[ fillcolor=aquamarine3]".format(person))
        #                graph_group[group].append("{}[ fillcolor=darkgrey]".format(i))
         #   else:
         #       not_s_smkoes.append("s_smokes({})".format(person))
         #       # not_s_smokes_vbl.append("not_ s_smokes({})".format(person))

    curr_dir = os.getcwd()
    Path(curr_dir + "\\result\\graphs").mkdir(parents=True, exist_ok=True)
    graph_file_name = curr_dir + "/result/graphs/{}_{}_graph_{}_{}.dot".format(prefix,fixedParam,size,datetime.datetime.now().strftime('%Y-%m-%d_%H%M%S'))
    print(graph_file_name)
    log_file_name = curr_dir + "/result/{}_log.txt".format(prefix)
    os.makedirs(os.path.dirname(log_file_name), exist_ok=True)
    with open(log_file_name, "w+") as f:
        f.write("Prefix: {}\n".format(prefix))
        f.write("Size: {}\n".format(size))
        f.write("Group count: {}\n".format(group_count))
        f.write("Group size: {}\n".format(group_size))
        f.write("\n")
        for i in range(len(groups)):
            index_from = 0 + i * group_size
            index_to = (i + 1) * group_size - 1
            f.write("Group #{} ({}-{}): {}\n".format(i, index_from, index_to, groups[i]))
        f.close()

    facts_file_name = curr_dir + "/result/{}/{}_facts.txt".format(prefix, prefix)
    os.makedirs(os.path.dirname(facts_file_name), exist_ok=True)
    with open(facts_file_name, "w+") as f:
        f.write("useStdLogicVariables: true.\n")
        for i in range(len(friends)):
            f.write(str(friends[i]) + ".\n")
        for i in range(len(smoking)):
            f.write(str(smoking[i]) + ".\n")
        for i in range(len(s_smokes)):
            f.write(str(s_smokes[i]) + ".\n")
        for j in range(len(not_s_smkoes)):
            f.write(str(not_s_smkoes[j]) + "\n")
        f.close()

    #pos_file_name = curr_dir + "/result/{}/{}_pos.txt".format(prefix, prefix)
    #os.makedirs(os.path.dirname(pos_file_name), exist_ok=True)
    #with open(pos_file_name, "w+") as f:
    #     for i in range(len(cancer)):
    #         f.write(str(cancer[i]) + ".\n")
    #    for i in range(len(smoking)):
    #        f.write(str(smoking[i]) + ".\n")
    #     for j in range(len(not_s_smokes_vbl)):
    #        f.write(str(not_s_smokes_vbl[j]) + "\n")
    #    f.close()

    #neg_file_name = curr_dir + "/result/{}/{}_neg.txt".format(prefix, prefix)
    #os.makedirs(os.path.dirname(neg_file_name), exist_ok=True)
    #with open(neg_file_name, "w+") as f:
        # for i in range(len(not_cancer)):
        #    f.write(str(not_cancer[i]) + ".\n")
    #    for i in range(len(not_smoking)):
    #        f.write(str(not_smoking[i]) + ".\n")
    #    f.close()

    with open(graph_file_name, "w+") as f:
        groupString = []
        for i in graph_group:
            if groups[i] == "smoke":
                color = "darksalmon"
            else:
                color = "darkseagreen1"
            body = """label = "Group_{}"
            		style=filled
            		color={}
            		node[style=filled]""".format(i,color)
            groupsBody = '''\nsubgraph cluster_{}'''.format(i) +"{" + body
            for j in range(len(graph_group[i])):
                groupsBody = groupsBody + graph_group[i].pop(0) + "\n"
            groupsBody = groupsBody + "}\n"
            groupString.append(groupsBody)
        graph_friends = ' \n'.join(graph_friends)
        groupString = ' \n'.join(groupString)
        wholeGraph = "graph graphSmokers {" + groupString + graph_friends + "}"
        #print(wholeGraph)
        f.write(wholeGraph)

    print("Dataset {} ({}) created!".format(prefix, size))
    return


def get_group_for_person(person_index, group_size):
    return person_index // group_size


def get_person_name(pers_index):
    return "Person_{}".format(pers_index)


# datenset größe, prefix, anzahl gruppen, gruppengröße
#create_set(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5])
create_set(70, "train", 5, 'None')
create_set(100, "test", 5, 'None')
